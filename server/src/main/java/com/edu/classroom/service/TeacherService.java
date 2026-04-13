package com.edu.classroom.service;

import com.edu.classroom.dto.teacher.TeacherCourseCreateRequest;
import com.edu.classroom.dto.teacher.TeacherResourceCreateRequest;
import com.edu.classroom.dto.teacher.TeacherResourceUpdateRequest;
import com.edu.classroom.dto.teacher.TeacherAssignmentCreateRequest;
import com.edu.classroom.dto.teacher.CheckinStatsResponse;
import com.edu.classroom.dto.teacher.CourseStatsOverviewResponse;
import com.edu.classroom.dto.teacher.HomeworkCompletionStatsResponse;
import com.edu.classroom.dto.teacher.ScoreDistributionResponse;
import com.edu.classroom.dto.teacher.StudentLearningMonitorDto;
import com.edu.classroom.dto.teacher.TeacherStudentGradeItem;
import com.edu.classroom.dto.exam.TeacherExamPaperCreateRequest;
import com.edu.classroom.dto.exam.TeacherExamPaperWithQuestionsCreateRequest;
import com.edu.classroom.dto.exam.TeacherExamQuestionCreateRequest;
import com.edu.classroom.dto.exam.TeacherExamAttemptListItem;
import com.edu.classroom.dto.exam.TeacherExamAttemptGradingResponse;
import com.edu.classroom.dto.exam.TeacherExamGradingQuestionDto;
import com.edu.classroom.dto.exam.TeacherExamGradeSaveRequest;
import com.edu.classroom.dto.exam.ExamPaperDetailResponse;
import com.edu.classroom.dto.exam.TeacherExamQuestionAnalysisDto;
import com.edu.classroom.dto.exam.TeacherExamAttemptListItem;
import com.edu.classroom.entity.Course;
import com.edu.classroom.entity.CourseGradeRule;
import com.edu.classroom.entity.CourseResource;
import com.edu.classroom.entity.Assignment;
import com.edu.classroom.entity.SysUser;
import com.edu.classroom.mapper.CourseMapper;
import com.edu.classroom.mapper.CourseGradeRuleMapper;
import com.edu.classroom.mapper.CourseMemberMapper;
import com.edu.classroom.mapper.AssignmentMapper;
import com.edu.classroom.mapper.ResourceMapper;
import com.edu.classroom.mapper.CheckinMapper;
import com.edu.classroom.mapper.GradeMapper;
import com.edu.classroom.mapper.ExamMapper;
import com.edu.classroom.mapper.SysUserMapper;
import com.edu.classroom.mapper.LearningProgressMapper;
import com.edu.classroom.mapper.RecommendationMapper;
import com.edu.classroom.entity.AssignmentGrade;
import com.edu.classroom.entity.ExamAttempt;
import com.edu.classroom.entity.Checkin;
import com.edu.classroom.entity.CheckinRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
public class TeacherService {
  private static final String COURSE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final long MAX_UPLOAD_SIZE = 500L * 1024L * 1024L;
  private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mkv", "mov", "wmv", "webm", "flv", "m4v");
  private static final double DEF_ASSIGN_W = 0.70;
  private static final double DEF_CHECKIN_W = 0.20;
  private static final double DEF_RESOURCE_W = 0.10;
  private static final double DEF_EXAM_W = 0.0;
  private final CourseMapper courseMapper;
  private final CourseGradeRuleMapper courseGradeRuleMapper;
  private final CourseMemberMapper courseMemberMapper;
  private final ResourceMapper resourceMapper;
  private final AssignmentMapper assignmentMapper;
  private final CheckinMapper checkinMapper;
  private final GradeMapper gradeMapper;
  private final ExamMapper examMapper;
  private final LearningProgressMapper learningProgressMapper;
  private final RecommendationMapper recommendationMapper;
  private final SysUserMapper sysUserMapper;
  @Value("${file.upload-dir:uploads}")
  private String uploadDir;
  private final Random random = new Random();

  public TeacherService(CourseMapper courseMapper,
                         CourseGradeRuleMapper courseGradeRuleMapper,
                         CourseMemberMapper courseMemberMapper,
                         ResourceMapper resourceMapper,
                         AssignmentMapper assignmentMapper,
                         CheckinMapper checkinMapper,
                         GradeMapper gradeMapper,
                         ExamMapper examMapper,
                         LearningProgressMapper learningProgressMapper,
                         RecommendationMapper recommendationMapper,
                         SysUserMapper sysUserMapper) {
    this.courseMapper = courseMapper;
    this.courseGradeRuleMapper = courseGradeRuleMapper;
    this.courseMemberMapper = courseMemberMapper;
    this.resourceMapper = resourceMapper;
    this.assignmentMapper = assignmentMapper;
    this.checkinMapper = checkinMapper;
    this.gradeMapper = gradeMapper;
    this.examMapper = examMapper;
    this.learningProgressMapper = learningProgressMapper;
    this.recommendationMapper = recommendationMapper;
    this.sysUserMapper = sysUserMapper;
  }

  public List<Course> listTeachingCourses(Long teacherId) {
    return courseMapper.listTeaching(teacherId);
  }

  public Course getMyCourse(Long teacherId, Long courseId) {
    Course c = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (c == null) {
      throw new RuntimeException("课程不存在或无权限");
    }
    attachGradeWeights(c);
    return c;
  }

  private void attachGradeWeights(Course course) {
    if (course == null || course.getId() == null) return;
    CourseGradeRule rule = courseGradeRuleMapper.findByCourseId(course.getId());
    if (rule != null) {
      course.setGradeAssignmentWeight(rule.getAssignmentWeight());
      course.setGradeCheckinWeight(rule.getCheckinWeight());
      course.setGradeResourceWeight(rule.getResourceWeight());
      course.setGradeExamWeight(rule.getExamWeight() != null ? rule.getExamWeight() : DEF_EXAM_W);
    } else {
      course.setGradeAssignmentWeight(DEF_ASSIGN_W);
      course.setGradeCheckinWeight(DEF_CHECKIN_W);
      course.setGradeResourceWeight(DEF_RESOURCE_W);
      course.setGradeExamWeight(DEF_EXAM_W);
    }
  }

  private void validateGradeWeights(double a, double c, double r, double e) {
    if (a < 0 || c < 0 || r < 0 || e < 0 || a > 1 || c > 1 || r > 1 || e > 1) {
      throw new RuntimeException("成绩占比须在 0～1 之间（即每项 0%～100%）");
    }
    double sum = a + c + r + e;
    if (Math.abs(sum - 1.0) > 0.02) {
      throw new RuntimeException("作业、签到、资源、考试四项占比之和须为 1（合计 100%）");
    }
  }

  private double[] resolveWeightsForCreate(TeacherCourseCreateRequest req) {
    if (req.getAssignmentWeight() == null && req.getCheckinWeight() == null && req.getResourceWeight() == null && req.getExamWeight() == null) {
      return new double[] {DEF_ASSIGN_W, DEF_CHECKIN_W, DEF_RESOURCE_W, DEF_EXAM_W};
    }
    if (req.getAssignmentWeight() == null || req.getCheckinWeight() == null || req.getResourceWeight() == null || req.getExamWeight() == null) {
      throw new RuntimeException("请同时填写作业、签到、资源、考试四项成绩占比，或全部留空使用默认 70% / 20% / 10% / 0%");
    }
    double a = req.getAssignmentWeight();
    double c = req.getCheckinWeight();
    double r = req.getResourceWeight();
    double e = req.getExamWeight();
    validateGradeWeights(a, c, r, e);
    return new double[] {a, c, r, e};
  }

  private int countNonNullWeights(TeacherCourseCreateRequest req) {
    int n = 0;
    if (req.getAssignmentWeight() != null) n++;
    if (req.getCheckinWeight() != null) n++;
    if (req.getResourceWeight() != null) n++;
    if (req.getExamWeight() != null) n++;
    return n;
  }

  public Course createCourse(Long teacherId, TeacherCourseCreateRequest req) {
    if (req == null || !StringUtils.hasText(req.getName())) {
      throw new RuntimeException("课程名称不能为空");
    }
    String name = req.getName().trim();
    if (name.length() > 100) {
      throw new RuntimeException("课程名称不能超过100个字符");
    }

    Course course = new Course();
    course.setName(name);
    course.setCourseCode(generateUniqueCourseCode());
    course.setIntro(trimToNull(req.getIntro(), 500));
    course.setCategory(trimToNull(req.getCategory(), 50));
    course.setCoverUrl(trimToNull(req.getCoverUrl(), 255));
    course.setTeacherId(teacherId);

    courseMapper.insertCourse(course);
    Course created = courseMapper.findByCode(course.getCourseCode());
    if (created == null) {
      throw new RuntimeException("课程创建失败，请稍后重试");
    }
    courseMapper.upsertTeacherMember(created.getId(), teacherId);
    double[] w = resolveWeightsForCreate(req);
    courseGradeRuleMapper.upsert(created.getId(), w[0], w[1], w[2], w[3]);
    attachGradeWeights(created);
    return created;
  }

  public Course updateCourse(Long teacherId, Long courseId, TeacherCourseCreateRequest req) {
    if (req == null || !StringUtils.hasText(req.getName())) {
      throw new RuntimeException("课程名称不能为空");
    }
    String name = req.getName().trim();
    if (name.length() > 100) {
      throw new RuntimeException("课程名称不能超过100个字符");
    }
    Course exists = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (exists == null) {
      throw new RuntimeException("课程不存在或无权限");
    }

    Course toUpdate = new Course();
    toUpdate.setId(courseId);
    toUpdate.setTeacherId(teacherId);
    toUpdate.setName(name);
    toUpdate.setIntro(trimToNull(req.getIntro(), 500));
    toUpdate.setCategory(trimToNull(req.getCategory(), 50));
    toUpdate.setCoverUrl(trimToNull(req.getCoverUrl(), 255));
    int rows = courseMapper.updateCourseByTeacher(toUpdate);
    if (rows <= 0) {
      throw new RuntimeException("更新失败，请重试");
    }
    int wn = countNonNullWeights(req);
    if (wn == 4) {
      validateGradeWeights(req.getAssignmentWeight(), req.getCheckinWeight(), req.getResourceWeight(), req.getExamWeight());
      courseGradeRuleMapper.upsert(courseId, req.getAssignmentWeight(), req.getCheckinWeight(), req.getResourceWeight(), req.getExamWeight());
    } else if (wn != 0) {
      throw new RuntimeException("请同时填写作业、签到、资源、考试四项成绩占比，或全部留空以保持原规则");
    }
    Course updated = courseMapper.findByIdAndTeacher(courseId, teacherId);
    attachGradeWeights(updated);
    return updated;
  }

  public void deleteCourse(Long teacherId, Long courseId) {
    int rows = courseMapper.softDeleteByTeacher(courseId, teacherId);
    if (rows <= 0) {
      throw new RuntimeException("课程不存在或无权限");
    }
  }

  public List<SysUser> listCourseStudents(Long teacherId, Long courseId) {
    return courseMemberMapper.listCourseStudents(teacherId, courseId);
  }

  public void removeCourseStudent(Long teacherId, Long courseId, Long studentId) {
    if (studentId == null) throw new RuntimeException("学生ID不能为空");
    int rows = courseMemberMapper.softRemoveCourseStudent(teacherId, courseId, studentId);
    if (rows <= 0) {
      throw new RuntimeException("学生不存在或无权限");
    }
  }

  public List<CourseResource> listTeacherResources(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    return resourceMapper.listByCourseAndTeacher(teacherId, courseId);
  }

  public CourseResource createResource(Long teacherId, Long courseId, TeacherResourceCreateRequest req) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (req == null || !StringUtils.hasText(req.getTitle())) {
      throw new RuntimeException("资源标题不能为空");
    }
    String title = req.getTitle().trim();
    if (title.length() > 200) throw new RuntimeException("资源标题不能超过200个字符");

    String description = trimToNull(req.getDescription(), 500);
    String category = trimToNull(req.getCategory(), 20);
    if (category == null) throw new RuntimeException("资源分类不能为空");

    if (!StringUtils.hasText(req.getFileUrl())) throw new RuntimeException("资源文件URL不能为空");
    if (!StringUtils.hasText(req.getFileName())) throw new RuntimeException("资源文件名称不能为空");
    if (!StringUtils.hasText(req.getFileType())) throw new RuntimeException("资源文件类型不能为空");
    long fileSize = req.getFileSize() == null ? 0L : req.getFileSize();
    if (fileSize < 0) throw new RuntimeException("资源文件大小不合法");

    int rows = resourceMapper.insertResource(
      courseId,
      teacherId,
      title,
      description,
      category,
      req.getFileUrl().trim(),
      req.getFileName().trim(),
      fileSize,
      req.getFileType().trim()
    );
    if (rows <= 0) throw new RuntimeException("资源发布失败，请重试");

    // insert 后直接返回列表中的最新一条不稳定，所以简单返回 courseResource 由前端展示即可
    // 这里补一个最小 CourseResource 供前端使用
    CourseResource r = new CourseResource();
    r.setCourseId(courseId);
    r.setTitle(title);
    r.setDescription(description);
    r.setCategory(category);
    r.setFileUrl(req.getFileUrl().trim());
    r.setFileName(req.getFileName().trim());
    r.setFileSize(fileSize);
    r.setFileType(req.getFileType().trim());
    r.setUploaderId(teacherId);
    r.setStatus(1);
    return r;
  }

  public CourseResource updateResource(Long teacherId,
                                        Long courseId,
                                        Long resourceId,
                                        TeacherResourceUpdateRequest req) {
    if (req == null || !StringUtils.hasText(req.getTitle())) {
      throw new RuntimeException("资源标题不能为空");
    }
    String title = req.getTitle().trim();
    if (title.length() > 200) throw new RuntimeException("资源标题不能超过200个字符");

    String description = trimToNull(req.getDescription(), 500);
    String category = trimToNull(req.getCategory(), 20);
    if (category == null) throw new RuntimeException("资源分类不能为空");

    // 用更新行数来判断权限是否正确
    int rows = resourceMapper.updateResourceByTeacher(teacherId, courseId, resourceId, title, description, category);
    if (rows <= 0) {
      throw new RuntimeException("资源不存在或无权限");
    }

    // 由于没有提供按资源ID查询（实现轻量），这里直接返回一个最小对象供前端展示
    CourseResource r = new CourseResource();
    r.setId(resourceId);
    r.setCourseId(courseId);
    r.setTitle(title);
    r.setDescription(description);
    r.setCategory(category);
    return r;
  }

  public void deleteResource(Long teacherId, Long courseId, Long resourceId) {
    int rows = resourceMapper.softDeleteResourceByTeacher(teacherId, courseId, resourceId);
    if (rows <= 0) {
      throw new RuntimeException("资源不存在或无权限");
    }
  }

  public CourseResource uploadResourceFile(Long teacherId, Long courseId, MultipartFile file, String clientOriginalName) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (file == null || file.isEmpty()) throw new RuntimeException("请选择文件");
    if (file.getSize() > MAX_UPLOAD_SIZE) throw new RuntimeException("文件不能超过500MB");

    // 小程序 wx.uploadFile  multipart 里的文件名多为临时路径随机串，优先用客户端传来的真实文件名
    String originalName = StringUtils.hasText(clientOriginalName) ? clientOriginalName.trim() : file.getOriginalFilename();
    if (!StringUtils.hasText(originalName)) throw new RuntimeException("文件名无效");
    String safeName = sanitizeFileName(originalName);
    String ext = getExtension(safeName);
    String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
    // 允许上传视频（本地存储），视频播放需要 /api/files 支持 Range

    String storedName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : ("." + ext));
    try {
      Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
      Files.createDirectories(dirPath);
      Path target = dirPath.resolve(storedName);
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

      CourseResource res = new CourseResource();
      res.setCourseId(courseId);
      res.setFileUrl("/api/files/" + storedName);
      res.setFileName(safeName);
      res.setFileSize(file.getSize());
      res.setFileType(StringUtils.hasText(contentType) ? contentType : (ext.isEmpty() ? "application/octet-stream" : ext));
      return res;
    } catch (Exception e) {
      throw new RuntimeException("文件上传失败，请重试");
    }
  }

  private String generateUniqueCourseCode() {
    for (int i = 0; i < 10; i++) {
      String code = randomCode(6);
      if (courseMapper.findByCode(code) == null) {
        return code;
      }
    }
    throw new RuntimeException("生成课程码失败，请重试");
  }

  private String randomCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(COURSE_CODE_CHARS.charAt(random.nextInt(COURSE_CODE_CHARS.length())));
    }
    return sb.toString();
  }

  /** 4 位（1000–9999）或 5 位（10000–99999）数字签到码，全库唯一 */
  private String generateUniqueCheckinCode() {
    for (int i = 0; i < 40; i++) {
      String code = random.nextBoolean()
          ? String.valueOf(1000 + random.nextInt(9000))
          : String.valueOf(10000 + random.nextInt(90000));
      if (checkinMapper.countByCheckinCode(code) == 0) {
        return code;
      }
    }
    throw new RuntimeException("生成签到码失败，请重试");
  }

  private String trimToNull(String text, int maxLen) {
    if (!StringUtils.hasText(text)) {
      return null;
    }
    String trimmed = text.trim();
    if (trimmed.length() > maxLen) {
      throw new RuntimeException("字段长度超出限制");
    }
    return trimmed;
  }

  private String sanitizeFileName(String name) {
    String n = name.replace("\\", "_").replace("/", "_").replace("..", "_").trim();
    return n.isEmpty() ? "file" : n;
  }

  private String getExtension(String fileName) {
    int idx = fileName.lastIndexOf('.');
    if (idx < 0 || idx == fileName.length() - 1) return "";
    return fileName.substring(idx + 1).toLowerCase();
  }

  private boolean isVideo(String ext, String contentType) {
    if (VIDEO_EXTENSIONS.contains(ext)) return true;
    return contentType.startsWith("video/");
  }

  public List<Assignment> listTeacherAssignments(Long teacherId, Long courseId) {
    // 只允许查询自己任课课程
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    return assignmentMapper.listByCourseAndTeacher(teacherId, courseId);
  }

  public Assignment createAssignment(Long teacherId, Long courseId, TeacherAssignmentCreateRequest req) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (req == null || !StringUtils.hasText(req.getTitle())) {
      throw new RuntimeException("作业标题不能为空");
    }
    String title = req.getTitle().trim();
    if (title.length() > 200) throw new RuntimeException("作业标题不能超过200个字符");

    String content = trimToNull(req.getContent(), 5000);
    String dueAtStr = req.getDueAt();
    if (!StringUtils.hasText(dueAtStr)) throw new RuntimeException("截止时间不能为空");
    LocalDateTime dueAt = parseDueAt(dueAtStr.trim());

    Double totalScore = req.getTotalScore() == null ? 100.00 : req.getTotalScore();
    if (totalScore <= 0) throw new RuntimeException("总分必须大于0");

    String attachmentUrl = trimToNull(req.getAttachmentUrl(), 255);

    int rows = assignmentMapper.insertAssignment(
      courseId,
      title,
      content,
      dueAt,
      totalScore,
      teacherId,
      attachmentUrl
    );
    if (rows <= 0) throw new RuntimeException("创建作业失败，请重试");

    // 轻量返回最新一条不稳定；创建后前端会刷新列表即可
    Assignment a = new Assignment();
    a.setCourseId(courseId);
    a.setTitle(title);
    a.setContent(content);
    a.setDueAt(dueAt);
    a.setTotalScore(totalScore);
    a.setCreatorId(teacherId);
    a.setAttachmentUrl(attachmentUrl);
    a.setStatus(1);
    return a;
  }

  public Assignment getTeacherAssignment(Long teacherId, Long courseId, Long assignmentId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    Assignment a = assignmentMapper.findByIdAndTeacher(teacherId, courseId, assignmentId);
    if (a == null) throw new RuntimeException("作业不存在或无权限");
    return a;
  }

  public List<com.edu.classroom.dto.teacher.AssignmentSubmissionGradeDto> listTeacherAssignmentSubmissions(
    Long teacherId,
    Long courseId,
    Long assignmentId
  ) {
    // 先校验作业归属，避免返回其他人的数据
    getTeacherAssignment(teacherId, courseId, assignmentId);
    return assignmentMapper.listSubmissionsForTeacher(teacherId, courseId, assignmentId);
  }

  public void gradeTeacherSubmission(
    Long teacherId,
    Long courseId,
    Long assignmentId,
    Long submissionId,
    Double score,
    String comment
  ) {
    if (score == null) throw new RuntimeException("分数不能为空");
    if (score < 0) throw new RuntimeException("分数不能为负数");
    String c = trimToNull(comment, 1000);

    AssignmentGrade existing = assignmentMapper.findGradeBySubmission(submissionId);
    if (existing != null) {
      throw new RuntimeException("该提交已批改，不可修改");
    }

    // 通过 SQL 的 join 校验提交必须属于该教师/课程/作业，并且只允许已提交状态
    int rows = assignmentMapper.insertGradeByTeacher(teacherId, courseId, assignmentId, submissionId, score, c);
    if (rows <= 0) {
      throw new RuntimeException("提交不存在或无权限");
    }
  }

  private LocalDateTime parseDueAt(String dueAt) {
    // 允许后端接收: "yyyy-MM-dd HH:mm:ss"
    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    try {
      return LocalDateTime.parse(dueAt, f);
    } catch (Exception e) {
      throw new RuntimeException("截止时间格式错误，应为：YYYY-MM-DD HH:mm:ss");
    }
  }

  private LocalDateTime parseDateTimeOrNull(String text) {
    if (!StringUtils.hasText(text)) return null;
    return parseDueAt(text.trim());
  }

  public com.edu.classroom.entity.ExamPaper createExamPaper(Long teacherId, Long courseId, TeacherExamPaperCreateRequest req) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (req == null || !StringUtils.hasText(req.getTitle())) throw new RuntimeException("试卷标题不能为空");
    String title = req.getTitle().trim();
    if (title.length() > 200) throw new RuntimeException("试卷标题不能超过200个字符");

    Integer duration = req.getDurationMinutes();
    if (duration != null && duration <= 0) throw new RuntimeException("答题时长必须大于0");

    LocalDateTime startAt = parseDateTimeOrNull(req.getStartAt());
    LocalDateTime endAt = parseDateTimeOrNull(req.getEndAt());
    if (startAt != null && endAt != null && endAt.isBefore(startAt)) throw new RuntimeException("结束时间不能早于开始时间");

    int shuffleFlag = (req.getShuffleQuestions() != null && req.getShuffleQuestions()) ? 1 : 0;
    Long paperId = examMapper.genId();
    int rows = examMapper.insertPaper(paperId, courseId, title, duration, startAt, endAt, shuffleFlag, teacherId);
    if (rows <= 0) throw new RuntimeException("创建试卷失败，请重试");

    com.edu.classroom.entity.ExamPaper p = new com.edu.classroom.entity.ExamPaper();
    p.setId(paperId);
    p.setCourseId(courseId);
    p.setTitle(title);
    p.setDurationMinutes(duration);
    p.setStartAt(startAt);
    p.setEndAt(endAt);
    p.setShuffleQuestions(shuffleFlag);
    p.setCreatorId(teacherId);
    p.setStatus(0);
    return p;
  }

  @Transactional
  public com.edu.classroom.entity.ExamPaper createExamPaperWithQuestions(Long teacherId,
                                                                         Long courseId,
                                                                         TeacherExamPaperWithQuestionsCreateRequest req) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (req == null || !StringUtils.hasText(req.getTitle())) throw new RuntimeException("试卷标题不能为空");
    String title = req.getTitle().trim();
    if (title.length() > 200) throw new RuntimeException("试卷标题不能超过200个字符");

    Integer duration = req.getDurationMinutes();
    if (duration != null && duration <= 0) throw new RuntimeException("答题时长必须大于0");

    LocalDateTime startAt = parseDateTimeOrNull(req.getStartAt());
    LocalDateTime endAt = parseDateTimeOrNull(req.getEndAt());
    if (startAt != null && endAt != null && endAt.isBefore(startAt)) throw new RuntimeException("结束时间不能早于开始时间");

    int shuffleFlag = (req.getShuffleQuestions() != null && req.getShuffleQuestions()) ? 1 : 0;
    Long paperId = examMapper.genId();
    int rows = examMapper.insertPaper(paperId, courseId, title, duration, startAt, endAt, shuffleFlag, teacherId);
    if (rows <= 0) throw new RuntimeException("创建试卷失败，请重试");

    if (req.getQuestions() != null && !req.getQuestions().isEmpty()) {
      int qi = 0;
      for (TeacherExamQuestionCreateRequest qreq : req.getQuestions()) {
        qi++;
        if (qreq == null) continue; // 防御：前端数组里出现 undefined/null 时，JSON 里会变成 null
        if (qreq.getQType() == null) throw new RuntimeException("第" + qi + "题题型不能为空");
        if (!StringUtils.hasText(qreq.getStem())) throw new RuntimeException("题干不能为空");
        Double score = qreq.getScore() == null ? 0.0 : qreq.getScore();
        if (score < 0) throw new RuntimeException("分值不能为负数");

        String stem = qreq.getStem().trim();
        if (stem.length() > 5000) throw new RuntimeException("题干过长");

        String correct = trimToNull(qreq.getCorrectAnswer(), 200);
        Integer sortNo = qreq.getSortNo() == null ? 0 : qreq.getSortNo();

        Long qId = examMapper.genId();
        int qrows = examMapper.insertQuestion(qId, paperId, qreq.getQType(), stem, score, sortNo, correct);
        if (qrows <= 0) throw new RuntimeException("添加题目失败，请重试");

        if (qreq.getOptions() != null && !qreq.getOptions().isEmpty()) {
          int i = 1;
          for (TeacherExamQuestionCreateRequest.Option opt : qreq.getOptions()) {
            if (opt == null) continue;
            if (!StringUtils.hasText(opt.getKey()) || !StringUtils.hasText(opt.getText())) continue;
            Long optId = examMapper.genId();
            int orows = examMapper.insertOption(optId, qId, opt.getKey().trim(), opt.getText().trim(), i++);
            if (orows <= 0) throw new RuntimeException("添加选项失败，请重试");
          }
        }
      }
    }

    com.edu.classroom.entity.ExamPaper p = new com.edu.classroom.entity.ExamPaper();
    p.setId(paperId);
    p.setCourseId(courseId);
    p.setTitle(title);
    p.setDurationMinutes(duration);
    p.setStartAt(startAt);
    p.setEndAt(endAt);
    p.setShuffleQuestions(shuffleFlag);
    p.setCreatorId(teacherId);
    p.setStatus(0);
    return p;
  }

  public java.util.List<com.edu.classroom.entity.ExamPaper> listExamPapers(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    return examMapper.listTeacherPapers(teacherId, courseId);
  }

  public void publishExamPaper(Long teacherId, Long courseId, Long paperId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    int rows = examMapper.publishPaper(teacherId, courseId, paperId);
    if (rows <= 0) throw new RuntimeException("试卷不存在/无权限/已发布");
  }

  public com.edu.classroom.entity.ExamQuestion addExamQuestion(Long teacherId, Long courseId, Long paperId, TeacherExamQuestionCreateRequest req) {
    com.edu.classroom.entity.ExamPaper paper = examMapper.findPaperByTeacher(teacherId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");
    if (req == null || req.getQType() == null) throw new RuntimeException("题型不能为空");
    if (!StringUtils.hasText(req.getStem())) throw new RuntimeException("题干不能为空");
    Double score = req.getScore() == null ? 0.0 : req.getScore();
    if (score < 0) throw new RuntimeException("分值不能为负数");

    String stem = req.getStem().trim();
    if (stem.length() > 5000) throw new RuntimeException("题干过长");

    String correct = trimToNull(req.getCorrectAnswer(), 200);
    Integer sortNo = req.getSortNo() == null ? 0 : req.getSortNo();

    Long qId = examMapper.genId();
    int rows = examMapper.insertQuestion(qId, paperId, req.getQType(), stem, score, sortNo, correct);
    if (rows <= 0) throw new RuntimeException("添加题目失败，请重试");

    if (req.getOptions() != null && !req.getOptions().isEmpty()) {
      int i = 1;
      for (TeacherExamQuestionCreateRequest.Option opt : req.getOptions()) {
        if (opt == null) continue;
        if (!StringUtils.hasText(opt.getKey()) || !StringUtils.hasText(opt.getText())) continue;
        Long optId = examMapper.genId();
        examMapper.insertOption(optId, qId, opt.getKey().trim(), opt.getText().trim(), i++);
      }
    }

    com.edu.classroom.entity.ExamQuestion q = new com.edu.classroom.entity.ExamQuestion();
    q.setId(qId);
    q.setPaperId(paperId);
    q.setQType(req.getQType());
    q.setStem(stem);
    q.setScore(score);
    q.setSortNo(sortNo);
    q.setCorrectAnswer(correct);
    q.setStatus(1);
    return q;
  }

  public ExamPaperDetailResponse examPaperDetailForTeacher(Long teacherId, Long courseId, Long paperId) {
    com.edu.classroom.entity.ExamPaper paper = examMapper.findPaperByTeacher(teacherId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");

    java.util.List<com.edu.classroom.entity.ExamQuestion> qs = examMapper.listQuestionsWithCorrectByPaper(paperId);
    java.util.List<ExamPaperDetailResponse.Option> opts = examMapper.listOptionsByPaper(paperId);

    java.util.Map<Long, java.util.List<ExamPaperDetailResponse.Option>> optMap = new java.util.HashMap<>();
    if (opts != null) {
      for (ExamPaperDetailResponse.Option o : opts) {
        if (o.getQuestionId() == null) continue;
        optMap.computeIfAbsent(o.getQuestionId(), k -> new java.util.ArrayList<>()).add(o);
      }
    }

    java.util.List<ExamPaperDetailResponse.Question> qResp = new java.util.ArrayList<>();
    if (qs != null) {
      for (com.edu.classroom.entity.ExamQuestion q : qs) {
        ExamPaperDetailResponse.Question qr = new ExamPaperDetailResponse.Question();
        qr.setId(q.getId());
        qr.setQType(q.getQType());
        qr.setStem(q.getStem());
        qr.setScore(q.getScore());
        qr.setSortNo(q.getSortNo());
        qr.setCorrectAnswer(q.getCorrectAnswer());
        qr.setOptions(optMap.getOrDefault(q.getId(), java.util.List.of()));
        qResp.add(qr);
      }
    }

    ExamPaperDetailResponse resp = new ExamPaperDetailResponse();
    resp.setPaperId(paper.getId());
    resp.setCourseId(paper.getCourseId());
    resp.setTitle(paper.getTitle());
    resp.setDurationMinutes(paper.getDurationMinutes());
    resp.setStartAt(paper.getStartAt());
    resp.setEndAt(paper.getEndAt());
    resp.setStatus(paper.getStatus());
    resp.setShuffleQuestions(paper.getShuffleQuestions());
    resp.setQuestions(qResp);
    return resp;
  }

  /** 试卷分析：每题正确率/得分率 + 错题排行 */
  public Map<String, Object> examPaperAnalysis(Long teacherId, Long courseId, Long paperId) {
    com.edu.classroom.entity.ExamPaper paper = examMapper.findPaperByTeacher(teacherId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");

    List<TeacherExamQuestionAnalysisDto> raw = examMapper.listQuestionAnalysisByPaper(paperId);
    raw = raw == null ? List.of() : raw;

    List<Map<String, Object>> questions = new ArrayList<>();
    List<Map<String, Object>> wrongRank = new ArrayList<>();
    for (TeacherExamQuestionAnalysisDto r : raw) {
      int participants = r.getParticipantCount() == null ? 0 : r.getParticipantCount();
      double full = r.getFullScore() == null ? 0.0 : r.getFullScore();
      double avg = r.getAvgScore() == null ? 0.0 : r.getAvgScore();
      int correct = r.getCorrectCount() == null ? 0 : r.getCorrectCount();
      Integer qt = r.getQType();
      boolean objective = qt != null && qt != 5;

      double correctRate = (participants > 0 && objective) ? (double) correct / participants : -1.0;
      double scoreRate = (participants > 0 && full > 0) ? (avg / full) : 0.0;
      int wrongCount = objective ? Math.max(0, participants - correct) : 0;

      Map<String, Object> row = new HashMap<>();
      row.put("questionId", r.getQuestionId());
      row.put("qType", qt);
      row.put("stem", r.getStem());
      row.put("fullScore", full);
      row.put("participantCount", participants);
      row.put("avgScore", Math.round(avg * 10.0) / 10.0);
      row.put("correctCount", correct);
      row.put("wrongCount", wrongCount);
      row.put("correctRate", objective ? Math.round(correctRate * 1000.0) / 10.0 : null); // 百分比
      row.put("scoreRate", Math.round(scoreRate * 1000.0) / 10.0); // 百分比
      questions.add(row);

      if (objective && participants > 0) {
        Map<String, Object> wr = new HashMap<>(row);
        wrongRank.add(wr);
      }
    }

    // 错题排行：先按错误人数降序，再按正确率升序
    wrongRank.sort((a, b) -> {
      int wa = ((Number) a.getOrDefault("wrongCount", 0)).intValue();
      int wb = ((Number) b.getOrDefault("wrongCount", 0)).intValue();
      if (wa != wb) return wb - wa;
      double ra = a.get("correctRate") == null ? 101 : ((Number) a.get("correctRate")).doubleValue();
      double rb = b.get("correctRate") == null ? 101 : ((Number) b.get("correctRate")).doubleValue();
      return Double.compare(ra, rb);
    });
    if (wrongRank.size() > 10) wrongRank = wrongRank.subList(0, 10);

    Map<String, Object> resp = new HashMap<>();
    resp.put("paperId", paperId);
    resp.put("paperTitle", paper.getTitle());
    resp.put("questions", questions);
    resp.put("wrongRank", wrongRank);
    return resp;
  }

  /** 导出成绩表（xls：HTML 表格，Excel 可直接打开，无额外依赖） */
  public byte[] exportExamScoresExcel(Long teacherId, Long courseId, Long paperId) {
    com.edu.classroom.entity.ExamPaper paper = examMapper.findPaperByTeacher(teacherId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");

    List<TeacherExamAttemptListItem> list = examMapper.listSubmittedAttemptsForTeacher(teacherId, courseId, paperId);
    list = list == null ? List.of() : list;

    try {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      StringBuilder sb = new StringBuilder(16_384);
      sb.append("<html><head><meta charset=\"UTF-8\"></head><body>");
      sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
      sb.append("<tr>")
        .append("<th>序号</th>")
        .append("<th>学号/账号</th>")
        .append("<th>姓名</th>")
        .append("<th>总分</th>")
        .append("<th>客观分</th>")
        .append("<th>提交时间</th>")
        .append("</tr>");
      int i = 1;
      for (TeacherExamAttemptListItem it : list) {
        String username = it.getStudentUsername() == null ? "" : it.getStudentUsername();
        String name = it.getStudentName() == null ? "" : it.getStudentName();
        double total = it.getTotalScore() == null ? 0.0 : it.getTotalScore();
        double obj = it.getObjectiveScore() == null ? 0.0 : it.getObjectiveScore();
        LocalDateTime sub = it.getSubmittedAt();
        String subText = sub == null ? "" : dtf.format(sub);
        sb.append("<tr>")
          .append("<td>").append(i++).append("</td>")
          .append("<td>").append(escapeHtml(username)).append("</td>")
          .append("<td>").append(escapeHtml(name)).append("</td>")
          .append("<td>").append(total).append("</td>")
          .append("<td>").append(obj).append("</td>")
          .append("<td>").append(escapeHtml(subText)).append("</td>")
          .append("</tr>");
      }
      sb.append("</table></body></html>");
      return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("生成Excel失败");
    }
  }

  /** 导出课程总评成绩表（xls：HTML 表格，Excel 可直接打开，无额外依赖） */
  public byte[] exportCourseFinalScoresExcel(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    List<TeacherStudentGradeItem> list = gradeMapper.listStudentGradesForCourse(teacherId, courseId);
    list = list == null ? List.of() : list;

    try {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      StringBuilder sb = new StringBuilder(16_384);
      sb.append("<html><head><meta charset=\"UTF-8\"></head><body>");
      sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
      sb.append("<tr>")
        .append("<th>序号</th>")
        .append("<th>姓名</th>")
        .append("<th>作业成绩</th>")
        .append("<th>签到成绩</th>")
        .append("<th>资源成绩</th>")
        .append("<th>考试成绩</th>")
        .append("<th>总评成绩</th>")
        .append("<th>计算时间</th>")
        .append("</tr>");
      int i = 1;
      for (TeacherStudentGradeItem it : list) {
        String name = it.getStudentName() == null ? "" : it.getStudentName();
        String assignment = it.getAssignmentScore() == null ? "教师未评分" : String.valueOf(it.getAssignmentScore());
        String checkin = it.getCheckinScore() == null ? "教师未评分" : String.valueOf(it.getCheckinScore());
        String resource = it.getResourceScore() == null ? "教师未评分" : String.valueOf(it.getResourceScore());
        String exam = it.getExamScore() == null ? "教师未评分" : String.valueOf(it.getExamScore());
        String finalScore = it.getFinalScore() == null ? "教师未评分" : String.valueOf(it.getFinalScore());
        LocalDateTime calc = it.getCalculatedAt();
        String calcText = calc == null ? "" : dtf.format(calc);
        sb.append("<tr>")
          .append("<td>").append(i++).append("</td>")
          .append("<td>").append(escapeHtml(name)).append("</td>")
          .append("<td>").append(escapeHtml(assignment)).append("</td>")
          .append("<td>").append(escapeHtml(checkin)).append("</td>")
          .append("<td>").append(escapeHtml(resource)).append("</td>")
          .append("<td>").append(escapeHtml(exam)).append("</td>")
          .append("<td>").append(escapeHtml(finalScore)).append("</td>")
          .append("<td>").append(escapeHtml(calcText)).append("</td>")
          .append("</tr>");
      }
      sb.append("</table></body></html>");
      return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("生成Excel失败");
    }
  }

  private static String escapeHtml(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
  }

  public List<TeacherExamAttemptListItem> listExamAttemptsForGrading(Long teacherId, Long courseId, Long paperId) {
    if (examMapper.findPaperByTeacher(teacherId, courseId, paperId) == null) {
      throw new RuntimeException("试卷不存在或无权限");
    }
    return examMapper.listSubmittedAttemptsForTeacher(teacherId, courseId, paperId);
  }

  public TeacherExamAttemptGradingResponse getExamAttemptForGrading(Long teacherId, Long courseId, Long paperId, Long attemptId) {
    com.edu.classroom.entity.ExamPaper paper = examMapper.findPaperByTeacher(teacherId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");
    ExamAttempt att = examMapper.findAttemptForTeacher(teacherId, courseId, paperId, attemptId);
    if (att == null || att.getStatus() == null || att.getStatus() != 2) {
      throw new RuntimeException("答卷不存在或未提交");
    }
    List<TeacherExamGradingQuestionDto> qs = examMapper.listGradingQuestionsForAttempt(paperId, attemptId);
    if (qs != null) {
      for (TeacherExamGradingQuestionDto q : qs) {
        q.setGradable(true);
      }
    }
    TeacherExamAttemptGradingResponse resp = new TeacherExamAttemptGradingResponse();
    resp.setPaperId(paperId);
    resp.setPaperTitle(paper.getTitle());
    resp.setAttemptId(attemptId);
    resp.setStudentId(att.getStudentId());
    resp.setStudentName(displayUserName(att.getStudentId()));
    resp.setSubmittedAt(att.getSubmittedAt());
    resp.setTotalScore(att.getTotalScore());
    resp.setObjectiveScore(att.getObjectiveScore());
    resp.setQuestions(qs == null ? List.of() : qs);
    return resp;
  }

  public TeacherExamAttemptGradingResponse saveExamAttemptGrades(Long teacherId, Long courseId, Long paperId, Long attemptId,
                                                                   TeacherExamGradeSaveRequest req) {
    if (examMapper.findPaperByTeacher(teacherId, courseId, paperId) == null) {
      throw new RuntimeException("试卷不存在或无权限");
    }
    ExamAttempt att = examMapper.findAttemptForTeacher(teacherId, courseId, paperId, attemptId);
    if (att == null || att.getStatus() == null || att.getStatus() != 2) {
      throw new RuntimeException("答卷不存在或未提交");
    }
    if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
      throw new RuntimeException("请至少提交一题的得分");
    }
    List<com.edu.classroom.entity.ExamQuestion> allq = examMapper.listQuestionsWithCorrectByPaper(paperId);
    Map<Long, com.edu.classroom.entity.ExamQuestion> qMap = new HashMap<>();
    if (allq != null) {
      for (com.edu.classroom.entity.ExamQuestion q : allq) {
        qMap.put(q.getId(), q);
      }
    }
    for (TeacherExamGradeSaveRequest.Item it : req.getItems()) {
      if (it == null || it.getQuestionId() == null) continue;
      com.edu.classroom.entity.ExamQuestion def = qMap.get(it.getQuestionId());
      if (def == null) throw new RuntimeException("题目不存在或不属于该试卷");
      double full = def.getScore() == null ? 0.0 : def.getScore();
      double sc = it.getScore() == null ? 0.0 : it.getScore();
      if (sc < 0 || sc > full + 1e-6) {
        throw new RuntimeException("得分不能为负且不能超过该题满分");
      }
      int up = examMapper.updateAnswerScoreForTeacher(teacherId, courseId, paperId, attemptId, it.getQuestionId(), sc);
      if (up <= 0) {
        String prev = examMapper.findAnswerText(attemptId, it.getQuestionId());
        examMapper.upsertAnswer(attemptId, it.getQuestionId(), prev != null ? prev : "", sc);
      }
    }
    Double total = examMapper.sumAnswerScores(attemptId);
    Double obj = examMapper.sumObjectiveAnswerScores(attemptId);
    if (total == null) total = 0.0;
    if (obj == null) obj = 0.0;
    int rows = examMapper.updateAttemptTotalScores(teacherId, courseId, paperId, attemptId, obj, total);
    if (rows <= 0) throw new RuntimeException("更新答卷总分失败");
    return getExamAttemptForGrading(teacherId, courseId, paperId, attemptId);
  }

  private String displayUserName(Long userId) {
    if (userId == null) return "";
    SysUser u = sysUserMapper.findById(userId);
    if (u == null) return "";
    if (StringUtils.hasText(u.getRealName())) return u.getRealName().trim();
    return u.getUsername() != null ? u.getUsername() : "";
  }

  public List<Checkin> listTeacherCheckins(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    return checkinMapper.listByCourseAndTeacher(teacherId, courseId);
  }

  public Checkin createCheckin(Long teacherId, Long courseId, String title, String endAtStr) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (!StringUtils.hasText(title)) throw new RuntimeException("签到标题不能为空");
    String t = trimToNull(title, 100);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = null;
    if (StringUtils.hasText(endAtStr)) {
      end = parseDueAt(endAtStr.trim());
      if (end.isBefore(start)) throw new RuntimeException("截止时间不能早于当前时间");
    }
    String checkinCode = generateUniqueCheckinCode();
    int rows = checkinMapper.insertCheckin(courseId, t, checkinCode, start, end, teacherId);
    if (rows <= 0) throw new RuntimeException("创建签到失败，请重试");
    Checkin c = checkinMapper.findByCheckinCode(checkinCode);
    if (c == null) throw new RuntimeException("创建签到失败，请重试");
    return c;
  }

  public void closeCheckin(Long teacherId, Long courseId, Long checkinId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    int rows = checkinMapper.closeByTeacher(teacherId, courseId, checkinId);
    if (rows <= 0) throw new RuntimeException("签到不存在或已结束或无权限");
  }

  public CheckinStatsResponse checkinStats(Long teacherId, Long courseId, Long checkinId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    Checkin checkin = checkinMapper.findById(checkinId);
    if (checkin == null || !courseId.equals(checkin.getCourseId())) {
      throw new RuntimeException("签到不存在或不属于该课程");
    }
    // 全部学生
    java.util.List<SysUser> all = courseMemberMapper.listCourseStudentsRaw(courseId);
    java.util.Map<Long, SysUser> allMap = new java.util.HashMap<>();
    for (SysUser u : all) {
      allMap.put(u.getId(), u);
    }
    // 已签到记录
    java.util.List<CheckinRecord> records = checkinMapper.listRecordsByCheckin(checkinId);
    java.util.Map<Long, CheckinRecord> recordMap = new java.util.HashMap<>();
    for (CheckinRecord r : records) {
      recordMap.put(r.getStudentId(), r);
    }

    java.util.List<CheckinStatsResponse.CheckedInStudent> checkedList = new java.util.ArrayList<>();
    java.util.List<CheckinStatsResponse.NotCheckedInStudent> notList = new java.util.ArrayList<>();

    for (SysUser u : all) {
      CheckinRecord r = recordMap.get(u.getId());
      String name = (u.getRealName() != null ? u.getRealName() : "") +
        "(" + (u.getUsername() != null ? u.getUsername() : "") + ")";
      if (r != null) {
        CheckinStatsResponse.CheckedInStudent cs = new CheckinStatsResponse.CheckedInStudent();
        cs.setStudentId(u.getId());
        cs.setStudentName(name);
        cs.setCheckedInAt(r.getCheckedInAt());
        checkedList.add(cs);
      } else {
        CheckinStatsResponse.NotCheckedInStudent ns = new CheckinStatsResponse.NotCheckedInStudent();
        ns.setStudentId(u.getId());
        ns.setStudentName(name);
        notList.add(ns);
      }
    }

    CheckinStatsResponse resp = new CheckinStatsResponse();
    resp.setCheckedIn(checkedList);
    resp.setNotCheckedIn(notList);
    return resp;
  }

  public CourseStatsOverviewResponse courseStatsOverview(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");

    int studentCount = courseMemberMapper.listCourseStudentsRaw(courseId).size();
    int signInCount = checkinMapper.countCheckinsByCourse(courseId) == null ? 0 : checkinMapper.countCheckinsByCourse(courseId);
    int totalSignInRecords = checkinMapper.countRecordsByCourse(courseId) == null ? 0 : checkinMapper.countRecordsByCourse(courseId);
    int homeworkCount = assignmentMapper.countAssignmentsByCourse(courseId) == null ? 0 : assignmentMapper.countAssignmentsByCourse(courseId);
    int totalHomeworkSubmitStudents = assignmentMapper.countDistinctSubmitStudentsByCourse(courseId) == null ? 0 : assignmentMapper.countDistinctSubmitStudentsByCourse(courseId);

    double avgAttendanceRate = 0.0;
    if (studentCount > 0 && signInCount > 0) {
      avgAttendanceRate = (double) totalSignInRecords / (studentCount * signInCount);
    }

    double avgHomeworkCompletionRate = 0.0;
    if (studentCount > 0 && homeworkCount > 0) {
      avgHomeworkCompletionRate = (double) totalHomeworkSubmitStudents / (studentCount * homeworkCount);
    }

    // 平均成绩：如果有课程总成绩表，以此为准；否则为 null
    Double avgScore = null;
    // 这里简单使用 course_final_grade 表的 final_score 平均值
    // 为保持 Mapper 简洁，这里用 GradeMapper 只负责查询单个，平均值可选后续扩展

    CourseStatsOverviewResponse resp = new CourseStatsOverviewResponse();
    resp.setCourseId(courseId);
    resp.setStudentCount(studentCount);
    resp.setSignInCount(signInCount);
    resp.setAvgAttendanceRate(avgAttendanceRate);
    resp.setHomeworkCount(homeworkCount);
    resp.setAvgHomeworkCompletionRate(avgHomeworkCompletionRate);
    resp.setAvgScore(avgScore);
    return resp;
  }

  /** 课程内每名选课学生的总评明细（无总评记录时各分项为 null） */
  public List<TeacherStudentGradeItem> listStudentGradesForCourse(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    List<TeacherStudentGradeItem> list = gradeMapper.listStudentGradesForCourse(teacherId, courseId);
    return list == null ? List.of() : list;
  }

  public List<HomeworkCompletionStatsResponse> homeworkCompletionStats(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    return assignmentMapper.listHomeworkCompletionStats(teacherId, courseId);
  }

  public ScoreDistributionResponse courseScoreDistribution(Long teacherId, Long courseId, Long homeworkId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");

    java.util.List<Double> scores;
    if (homeworkId != null) {
      scores = assignmentMapper.listAssignmentScoresForTeacher(teacherId, courseId, homeworkId);
    } else {
      scores = gradeMapper.listFinalScoresForCourse(teacherId, courseId);
    }

    int[] bucketsCount = new int[5]; // 0-59, 60-69, 70-79, 80-89, 90-100
    if (scores != null) {
      for (Double score : scores) {
        if (score == null) continue;
        if (score < 60) {
          bucketsCount[0]++;
        } else if (score < 70) {
          bucketsCount[1]++;
        } else if (score < 80) {
          bucketsCount[2]++;
        } else if (score < 90) {
          bucketsCount[3]++;
        } else {
          bucketsCount[4]++;
        }
      }
    }

    java.util.List<ScoreDistributionResponse.Bucket> buckets = new ArrayList<>();
    buckets.add(buildBucket("0-59", bucketsCount[0]));
    buckets.add(buildBucket("60-69", bucketsCount[1]));
    buckets.add(buildBucket("70-79", bucketsCount[2]));
    buckets.add(buildBucket("80-89", bucketsCount[3]));
    buckets.add(buildBucket("90-100", bucketsCount[4]));

    ScoreDistributionResponse resp = new ScoreDistributionResponse();
    resp.setCourseId(courseId);
    resp.setHomeworkId(homeworkId);
    resp.setBuckets(buckets);
    return resp;
  }

  private ScoreDistributionResponse.Bucket buildBucket(String range, int count) {
    ScoreDistributionResponse.Bucket b = new ScoreDistributionResponse.Bucket();
    b.setRange(range);
    b.setCount(count);
    return b;
  }

  public List<StudentLearningMonitorDto> monitorStudents(Long teacherId, Long courseId) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");

    List<SysUser> students = courseMemberMapper.listCourseStudentsRaw(courseId);
    int totalResources = resourceMapper.countResourcesByCourse(courseId) == null ? 0 : resourceMapper.countResourcesByCourse(courseId);
    int totalAssignments = assignmentMapper.countAssignmentsByCourse(courseId) == null ? 0 : assignmentMapper.countAssignmentsByCourse(courseId);
    int totalExams = examMapper.countPublishedPapersByCourse(courseId) == null ? 0 : examMapper.countPublishedPapersByCourse(courseId);
    int totalCheckins = checkinMapper.countCheckinsByCourse(courseId) == null ? 0 : checkinMapper.countCheckinsByCourse(courseId);

    List<StudentLearningMonitorDto> list = new ArrayList<>();
    for (SysUser s : students) {
      Long sid = s.getId();
      String name = (s.getRealName() != null ? s.getRealName() : "") + "(" + (s.getUsername() != null ? s.getUsername() : "") + ")";

      int completedResources = learningProgressMapper.countCompletedResources(courseId, sid) == null ? 0 : learningProgressMapper.countCompletedResources(courseId, sid);
      int submittedAssignments = assignmentMapper.countSubmittedAssignmentsByCourseAndStudent(courseId, sid) == null ? 0 : assignmentMapper.countSubmittedAssignmentsByCourseAndStudent(courseId, sid);
      int completedExams = examMapper.countCompletedExamPapersByCourseAndStudent(courseId, sid) == null ? 0 : examMapper.countCompletedExamPapersByCourseAndStudent(courseId, sid);
      int checkedIn = checkinMapper.countRecordsByCourseAndStudent(courseId, sid) == null ? 0 : checkinMapper.countRecordsByCourseAndStudent(courseId, sid);

      double resRate = (totalResources <= 0) ? 0.0 : (double) completedResources / totalResources;
      double hwRate = (totalAssignments <= 0) ? 0.0 : (double) submittedAssignments / totalAssignments;
      double examRate = (totalExams <= 0) ? 0.0 : (double) completedExams / totalExams;
      double attRate = (totalCheckins <= 0) ? 0.0 : (double) checkedIn / totalCheckins;

      String risk = calcRisk(resRate, hwRate, examRate, attRate);

      StudentLearningMonitorDto dto = new StudentLearningMonitorDto();
      dto.setStudentId(sid);
      dto.setStudentName(name);
      dto.setTotalResources(totalResources);
      dto.setCompletedResources(completedResources);
      dto.setResourceCompletionRate(resRate);
      dto.setTotalAssignments(totalAssignments);
      dto.setSubmittedAssignments(submittedAssignments);
      dto.setHomeworkCompletionRate(hwRate);
      dto.setTotalExams(totalExams);
      dto.setCompletedExams(completedExams);
      dto.setExamCompletionRate(examRate);
      dto.setTotalCheckins(totalCheckins);
      dto.setCheckedInCount(checkedIn);
      dto.setAttendanceRate(attRate);
      dto.setRiskLevel(risk);
      list.add(dto);
    }

    // 风险高的放前面
    list.sort((a, b) -> riskWeight(b.getRiskLevel()) - riskWeight(a.getRiskLevel()));
    return list;
  }

  public List<CourseResource> recommendResourcesForStudent(Long teacherId, Long courseId, Long studentId, Integer limit) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (studentId == null) throw new RuntimeException("学生ID不能为空");
    int lim = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);
    // 升级策略：优先推送分类匹配“薄弱项”的资源
    List<StudentLearningMonitorDto> monitors = monitorStudents(teacherId, courseId);
    StudentLearningMonitorDto current = null;
    for (StudentLearningMonitorDto m : monitors) {
      if (studentId.equals(m.getStudentId())) {
        current = m;
        break;
      }
    }
    List<CourseResource> base = resourceMapper.listRecommendedForStudent(courseId, studentId, 50);
    if (base == null || base.isEmpty()) return java.util.List.of();
    if (current == null) return base.subList(0, Math.min(lim, base.size()));

    String weakTag = weakestTag(current);
    base.sort((a, b) -> scoreResourceForWeakTag(b, weakTag) - scoreResourceForWeakTag(a, weakTag));
    return base.subList(0, Math.min(lim, base.size()));
  }

  public int pushRecommendations(Long teacherId, Long courseId, Long studentId, List<Long> resourceIds, String reason) {
    Course course = courseMapper.findByIdAndTeacher(courseId, teacherId);
    if (course == null) throw new RuntimeException("课程不存在或无权限");
    if (studentId == null) throw new RuntimeException("学生ID不能为空");
    if (resourceIds == null || resourceIds.isEmpty()) throw new RuntimeException("请选择要推送的资源");
    if (courseMemberMapper.countStudentInCourse(courseId, studentId) <= 0) throw new RuntimeException("学生不在课程中");
    String r = reason == null ? "" : reason.trim();
    if (r.length() > 500) throw new RuntimeException("推荐理由不能超过500字符");
    int count = 0;
    for (Long rid : resourceIds) {
      if (rid == null) continue;
      Long cid = resourceMapper.findCourseIdByResourceId(rid);
      if (cid == null || !courseId.equals(cid)) continue;
      count += recommendationMapper.insertPush(teacherId, courseId, studentId, rid, r);
    }
    return count;
  }

  private int riskWeight(String risk) {
    if ("HIGH".equalsIgnoreCase(risk)) return 3;
    if ("MEDIUM".equalsIgnoreCase(risk)) return 2;
    return 1;
  }

  private String calcRisk(double resRate, double hwRate, double examRate, double attRate) {
    // 四项完成率平均后换算为 0-100 的最终分
    double finalScore = (resRate + hwRate + examRate + attRate) / 4.0 * 100.0;
    if (finalScore < 60.0) return "HIGH";
    return "LOW";
  }

  private String weakestTag(StudentLearningMonitorDto d) {
    double res = d.getResourceCompletionRate() == null ? 0 : d.getResourceCompletionRate();
    double hw = d.getHomeworkCompletionRate() == null ? 0 : d.getHomeworkCompletionRate();
    double att = d.getAttendanceRate() == null ? 0 : d.getAttendanceRate();
    if (res <= hw && res <= att) return "RESOURCE";
    if (hw <= att) return "HOMEWORK";
    return "CHECKIN";
  }

  private int scoreResourceForWeakTag(CourseResource r, String weakTag) {
    String cat = (r.getCategory() == null ? "" : r.getCategory()).toLowerCase();
    String name = (r.getTitle() == null ? "" : r.getTitle()).toLowerCase();
    int score = 0;
    if ("RESOURCE".equals(weakTag)) {
      if (cat.contains("视频") || cat.contains("课件") || cat.contains("文档")) score += 3;
    } else if ("HOMEWORK".equals(weakTag)) {
      if (cat.contains("习题") || cat.contains("作业") || name.contains("练习")) score += 3;
    } else {
      if (cat.contains("课堂") || name.contains("总结") || name.contains("重点")) score += 2;
    }
    return score;
  }
}
