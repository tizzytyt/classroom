package com.edu.classroom.service;

import com.edu.classroom.dto.student.StudentAssignmentSubmitRequest;
import com.edu.classroom.dto.student.StudentCheckinHistoryDto;
import com.edu.classroom.entity.*;
import com.edu.classroom.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StudentService {
  private static final long MAX_ASSIGNMENT_UPLOAD = 500L * 1024 * 1024;
  private static final int MAX_ATTACHMENT_COUNT = 20;
  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Value("${file.upload-dir:uploads}")
  private String uploadDir;
  private final CourseMapper courseMapper;
  private final ResourceMapper resourceMapper;
  private final AssignmentMapper assignmentMapper;
  private final CheckinMapper checkinMapper;
  private final LearningProgressMapper learningProgressMapper;
  private final GradeMapper gradeMapper;
  private final CourseGradeRuleMapper courseGradeRuleMapper;
  private final CourseMemberMapper courseMemberMapper;
  private final ExamMapper examMapper;
  private final RecommendationMapper recommendationMapper;

  public StudentService(CourseMapper courseMapper,
                         ResourceMapper resourceMapper,
                         AssignmentMapper assignmentMapper,
                         CheckinMapper checkinMapper,
                         LearningProgressMapper learningProgressMapper,
                         GradeMapper gradeMapper,
                         CourseGradeRuleMapper courseGradeRuleMapper,
                         CourseMemberMapper courseMemberMapper,
                         ExamMapper examMapper,
                         RecommendationMapper recommendationMapper) {
    this.courseMapper = courseMapper;
    this.resourceMapper = resourceMapper;
    this.assignmentMapper = assignmentMapper;
    this.checkinMapper = checkinMapper;
    this.learningProgressMapper = learningProgressMapper;
    this.gradeMapper = gradeMapper;
    this.courseGradeRuleMapper = courseGradeRuleMapper;
    this.courseMemberMapper = courseMemberMapper;
    this.examMapper = examMapper;
    this.recommendationMapper = recommendationMapper;
  }

  public List<Course> listJoinedCourses(Long studentId) {
    return courseMapper.listJoined(studentId);
  }

  public Course joinByCode(Long studentId, String code, String joinType) {
    Course c = courseMapper.findByCode(code);
    if (c == null) throw new RuntimeException("课程不存在");
    courseMapper.upsertMember(c.getId(), studentId, joinType);
    return c;
  }

  public Course getCourse(Long studentId, Long courseId) {
    assertStudentInCourse(studentId, courseId);
    return courseMapper.findById(courseId);
  }

  public List<CourseResource> listResources(Long studentId, Long courseId) {
    assertStudentInCourse(studentId, courseId);
    return resourceMapper.listByCourse(courseId);
  }

  public void setProgress(Long resourceId, Long userId, Integer status, Integer percent) {
    Long courseId = resourceMapper.findCourseIdByResourceId(resourceId);
    if (courseId == null) throw new RuntimeException("资源不存在");
    assertStudentInCourse(userId, courseId);
    resourceMapper.upsertProgress(resourceId, userId, status, percent);
  }

  public List<Assignment> listAssignments(Long studentId, Long courseId) {
    assertStudentInCourse(studentId, courseId);
    return assignmentMapper.listByCourseForStudent(courseId, studentId);
  }

  public Map<String, Object> assignmentDetail(Long assignmentId, Long studentId) {
    Assignment a = assignmentMapper.findById(assignmentId);
    if (a == null) throw new RuntimeException("作业不存在");
    assertStudentInCourse(studentId, a.getCourseId());
    AssignmentSubmission sub = assignmentMapper.findSubmission(assignmentId, studentId);
    AssignmentGrade grade = null;
    if (sub != null) {
      grade = assignmentMapper.findGradeBySubmission(sub.getId());
    }
    Map<String, Object> res = new HashMap<>();
    res.put("assignment", a);
    res.put("submission", sub);
    res.put("grade", grade);
    if (sub != null && StringUtils.hasText(sub.getAttachmentUrls())) {
      try {
        res.put("submissionAttachmentUrls", jsonMapper.readValue(sub.getAttachmentUrls(), new TypeReference<List<String>>() {}));
      } catch (Exception e) {
        res.put("submissionAttachmentUrls", List.of());
      }
    } else {
      res.put("submissionAttachmentUrls", sub == null ? null : List.of());
    }
    return res;
  }

  public Map<String, Object> uploadAssignmentSubmissionFile(Long assignmentId, Long studentId, MultipartFile file, String clientOriginalName) {
    Assignment a = assignmentMapper.findById(assignmentId);
    if (a == null) throw new RuntimeException("作业不存在");
    assertStudentInCourse(studentId, a.getCourseId());
    LocalDateTime nowUpload = LocalDateTime.now();
    if (a.getDueAt() != null && nowUpload.isAfter(a.getDueAt())) {
      throw new RuntimeException("已超过截止时间，无法上传附件");
    }
    AssignmentSubmission subCheck = assignmentMapper.findSubmission(assignmentId, studentId);
    if (subCheck != null) {
      AssignmentGrade g = assignmentMapper.findGradeBySubmission(subCheck.getId());
      if (g != null) throw new RuntimeException("作业已批改，不可再上传或修改");
    }
    if (file == null || file.isEmpty()) throw new RuntimeException("请选择文件");
    if (file.getSize() > MAX_ASSIGNMENT_UPLOAD) throw new RuntimeException("文件不能超过500MB");
    String originalName = StringUtils.hasText(clientOriginalName) ? clientOriginalName.trim() : file.getOriginalFilename();
    if (!StringUtils.hasText(originalName)) throw new RuntimeException("文件名无效");
    String safeName = sanitizeFileName(originalName);
    String ext = getExtension(safeName);
    String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
    String storedName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : ("." + ext));
    try {
      Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
      Files.createDirectories(dirPath);
      Path target = dirPath.resolve(storedName);
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
      Map<String, Object> m = new HashMap<>();
      m.put("fileUrl", "/api/files/" + storedName);
      m.put("fileName", safeName);
      m.put("fileSize", file.getSize());
      m.put("fileType", StringUtils.hasText(contentType) ? contentType : (ext.isEmpty() ? "application/octet-stream" : ext));
      return m;
    } catch (Exception e) {
      throw new RuntimeException("文件上传失败，请重试");
    }
  }

  public void submitAssignment(Long assignmentId, Long studentId, StudentAssignmentSubmitRequest req) {
    if (req == null) {
      req = new StudentAssignmentSubmitRequest();
    }
    String text = req.getText() == null ? "" : req.getText().trim();
    if (text.length() > 10000) throw new RuntimeException("作业说明过长");
    List<String> urls = req.getAttachmentUrls() == null ? List.of() : req.getAttachmentUrls();
    if (urls.size() > MAX_ATTACHMENT_COUNT) throw new RuntimeException("附件数量不能超过" + MAX_ATTACHMENT_COUNT);
    for (String u : urls) {
      if (!isAllowedSubmissionFileUrl(u)) {
        throw new RuntimeException("附件地址不合法，请使用本页上传生成的文件");
      }
    }
    if (!StringUtils.hasText(text) && urls.isEmpty()) {
      throw new RuntimeException("请填写作业说明或上传至少一个附件");
    }
    Assignment a = assignmentMapper.findById(assignmentId);
    if (a == null) throw new RuntimeException("作业不存在");
    assertStudentInCourse(studentId, a.getCourseId());
    LocalDateTime now = LocalDateTime.now();
    if (a.getDueAt() != null && now.isAfter(a.getDueAt())) {
      throw new RuntimeException("已超过截止时间，无法提交");
    }
    AssignmentSubmission subCheck = assignmentMapper.findSubmission(assignmentId, studentId);
    if (subCheck != null) {
      AssignmentGrade g = assignmentMapper.findGradeBySubmission(subCheck.getId());
      if (g != null) throw new RuntimeException("作业已批改，不可修改");
    }
    Integer isLate = 0;
    String attachmentJson;
    try {
      attachmentJson = urls.isEmpty() ? null : jsonMapper.writeValueAsString(urls);
    } catch (Exception e) {
      throw new RuntimeException("附件数据错误");
    }
    assignmentMapper.upsertSubmissionText(assignmentId, studentId, text, isLate, attachmentJson);
  }

  private static boolean isAllowedSubmissionFileUrl(String u) {
    if (!StringUtils.hasText(u)) return false;
    String s = u.trim();
    return s.startsWith("/api/files/") && !s.contains("..");
  }

  private static String sanitizeFileName(String name) {
    String n = name.replace("\\", "_").replace("/", "_").replace("..", "_").trim();
    return n.isEmpty() ? "file" : n;
  }

  private static String getExtension(String fileName) {
    int idx = fileName.lastIndexOf('.');
    if (idx < 0 || idx == fileName.length() - 1) return "";
    return fileName.substring(idx + 1).toLowerCase();
  }

  public void checkin(Long checkinId, Long studentId, String source) {
    Checkin c = checkinMapper.findById(checkinId);
    if (c == null) throw new RuntimeException("签到不存在");
    assertStudentInCourse(studentId, c.getCourseId());
    if (c.getStatus() == null || c.getStatus() != 1) throw new RuntimeException("签到已结束");
    if (c.getEndAt() != null && LocalDateTime.now().isAfter(c.getEndAt())) throw new RuntimeException("签到已截止");
    CheckinRecord r = checkinMapper.findRecord(checkinId, studentId);
    if (r != null) throw new RuntimeException("已签到");
    checkinMapper.insertRecord(checkinId, studentId, source);
  }

  /** 学生通过 4～5 位数字签到码签到（与创建时生成的 5 位码一致） */
  public void checkinByCode(String codeRaw, Long studentId, String source) {
    if (!StringUtils.hasText(codeRaw)) throw new RuntimeException("请输入签到码");
    String code = codeRaw.trim().replaceAll("\\s+", "");
    if (!code.matches("\\d{4,5}")) throw new RuntimeException("签到码为4～5位数字");
    Checkin c = checkinMapper.findActiveByCheckinCode(code);
    if (c == null) throw new RuntimeException("签到码无效或签到已结束");
    checkin(c.getId(), studentId, source);
  }

  /** 学生端：签到历史记录（不返回签到码） */
  public List<StudentCheckinHistoryDto> listCheckinHistory(Long studentId, Long courseId) {
    assertStudentInCourse(studentId, courseId);
    return checkinMapper.listHistoryByCourseAndStudent(courseId, studentId);
  }

  /** 总评成绩 + 课程评分规则（占比），供学生端成绩页展示 */
  public Map<String, Object> finalGradeDetail(Long courseId, Long studentId) {
    assertStudentInCourse(studentId, courseId);
    CourseFinalGrade g = gradeMapper.findFinal(courseId, studentId);
    CourseGradeRule rule = courseGradeRuleMapper.findByCourseId(courseId);
    if (rule == null) {
      rule = new CourseGradeRule();
      rule.setAssignmentWeight(0.70);
      rule.setCheckinWeight(0.20);
      rule.setResourceWeight(0.10);
      rule.setExamWeight(0.0);
    } else if (rule.getExamWeight() == null) {
      rule.setExamWeight(0.0);
    }
    if (g == null) {
      g = buildRealtimeFinalGrade(courseId, studentId, rule);
    }
    Map<String, Object> m = new HashMap<>();
    m.put("grade", g);
    m.put("gradeRule", rule);
    return m;
  }

  /** 当 course_final_grade 尚未生成时，实时计算并返回一份成绩快照。 */
  private CourseFinalGrade buildRealtimeFinalGrade(Long courseId, Long studentId, CourseGradeRule rule) {
    int totalAssignments = assignmentMapper.countAssignmentsByCourse(courseId) == null ? 0 : assignmentMapper.countAssignmentsByCourse(courseId);
    int submittedAssignments = assignmentMapper.countSubmittedAssignmentsByCourseAndStudent(courseId, studentId) == null ? 0 : assignmentMapper.countSubmittedAssignmentsByCourseAndStudent(courseId, studentId);
    int totalResources = resourceMapper.countResourcesByCourse(courseId) == null ? 0 : resourceMapper.countResourcesByCourse(courseId);
    int completedResources = learningProgressMapper.countCompletedResources(courseId, studentId) == null ? 0 : learningProgressMapper.countCompletedResources(courseId, studentId);
    int totalCheckins = checkinMapper.countCheckinsByCourse(courseId) == null ? 0 : checkinMapper.countCheckinsByCourse(courseId);
    int checkedInCount = checkinMapper.countRecordsByCourseAndStudent(courseId, studentId) == null ? 0 : checkinMapper.countRecordsByCourseAndStudent(courseId, studentId);
    Double examAvg = examMapper.avgBestExamScoreByCourseAndStudent(courseId, studentId);

    double assignmentScore = totalAssignments <= 0 ? 0.0 : (submittedAssignments * 100.0 / totalAssignments);
    double resourceScore = totalResources <= 0 ? 0.0 : (completedResources * 100.0 / totalResources);
    double checkinScore = totalCheckins <= 0 ? 0.0 : (checkedInCount * 100.0 / totalCheckins);
    double examScore = examAvg == null ? 0.0 : examAvg;

    double aw = rule.getAssignmentWeight() == null ? 0.0 : rule.getAssignmentWeight();
    double cw = rule.getCheckinWeight() == null ? 0.0 : rule.getCheckinWeight();
    double rw = rule.getResourceWeight() == null ? 0.0 : rule.getResourceWeight();
    double ew = rule.getExamWeight() == null ? 0.0 : rule.getExamWeight();
    double finalScore = assignmentScore * aw + checkinScore * cw + resourceScore * rw + examScore * ew;

    CourseFinalGrade g = new CourseFinalGrade();
    g.setCourseId(courseId);
    g.setStudentId(studentId);
    g.setAssignmentScore(round2(assignmentScore));
    g.setCheckinScore(round2(checkinScore));
    g.setResourceScore(round2(resourceScore));
    g.setExamScore(round2(examScore));
    g.setFinalScore(round2(finalScore));
    g.setCalculatedAt(LocalDateTime.now());
    return g;
  }

  private static double round2(double n) {
    return Math.round(n * 100.0) / 100.0;
  }

  public List<ExamPaper> listAvailableExamPapers(Long studentId, Long courseId) {
    assertStudentInCourse(studentId, courseId);
    return examMapper.listStudentAvailablePapers(studentId, courseId);
  }

  public com.edu.classroom.dto.exam.ExamPaperDetailResponse examPaperDetailForStudent(Long studentId, Long courseId, Long paperId) {
    assertStudentInCourse(studentId, courseId);
    ExamPaper paper = examMapper.findPaperForStudent(studentId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");
    LocalDateTime now = LocalDateTime.now();
    if (paper.getStartAt() != null && now.isBefore(paper.getStartAt())) throw new RuntimeException("试卷未开始");
    if (paper.getEndAt() != null && now.isAfter(paper.getEndAt())) throw new RuntimeException("试卷已结束");

    List<ExamQuestion> qs = examMapper.listQuestionsByPaper(paperId);
    List<com.edu.classroom.dto.exam.ExamPaperDetailResponse.Option> opts = examMapper.listOptionsByPaper(paperId);
    Map<Long, List<com.edu.classroom.dto.exam.ExamPaperDetailResponse.Option>> optMap = new HashMap<>();
    if (opts != null) {
      for (var o : opts) {
        if (o.getQuestionId() == null) continue;
        optMap.computeIfAbsent(o.getQuestionId(), k -> new ArrayList<>()).add(o);
      }
    }

    List<com.edu.classroom.dto.exam.ExamPaperDetailResponse.Question> qResp = new ArrayList<>();
    if (qs != null) {
      for (ExamQuestion q : qs) {
        com.edu.classroom.dto.exam.ExamPaperDetailResponse.Question qr = new com.edu.classroom.dto.exam.ExamPaperDetailResponse.Question();
        qr.setId(q.getId());
        qr.setQType(q.getQType());
        qr.setStem(q.getStem());
        qr.setScore(q.getScore());
        qr.setSortNo(q.getSortNo());
        // 学生端不返回正确答案
        qr.setCorrectAnswer(null);
        qr.setOptions(optMap.getOrDefault(q.getId(), List.of()));
        qResp.add(qr);
      }
    }

    com.edu.classroom.dto.exam.ExamPaperDetailResponse resp = new com.edu.classroom.dto.exam.ExamPaperDetailResponse();
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

  public Long startExamAttempt(Long studentId, Long courseId, Long paperId) {
    assertStudentInCourse(studentId, courseId);
    ExamPaper paper = examMapper.findPaperForStudent(studentId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");
    LocalDateTime now = LocalDateTime.now();
    if (paper.getStartAt() != null && now.isBefore(paper.getStartAt())) throw new RuntimeException("试卷未开始");
    if (paper.getEndAt() != null && now.isAfter(paper.getEndAt())) throw new RuntimeException("试卷已结束");

    Long attemptId = examMapper.genId();
    int rows = examMapper.insertAttempt(attemptId, paperId, studentId);
    if (rows <= 0) throw new RuntimeException("开始答题失败，请重试");
    return attemptId;
  }

  public Map<String, Object> submitExamAttempt(Long studentId, Long courseId, Long paperId, Long attemptId, com.edu.classroom.dto.exam.StudentExamSubmitRequest req) {
    assertStudentInCourse(studentId, courseId);
    ExamPaper paper = examMapper.findPaperForStudent(studentId, courseId, paperId);
    if (paper == null) throw new RuntimeException("试卷不存在或无权限");

    ExamAttempt attempt = examMapper.findAttempt(attemptId, studentId);
    if (attempt == null || !paperId.equals(attempt.getPaperId())) throw new RuntimeException("答卷不存在或不属于该试卷");
    if (attempt.getStatus() != null && attempt.getStatus() == 2) throw new RuntimeException("答卷已提交");

    List<ExamQuestion> questions = examMapper.listQuestionsWithCorrectByPaper(paperId);
    Map<Long, ExamQuestion> qMap = new HashMap<>();
    if (questions != null) {
      for (ExamQuestion q : questions) qMap.put(q.getId(), q);
    }

    double objectiveScore = 0.0;
    if (req != null && req.getAnswers() != null) {
      for (com.edu.classroom.dto.exam.StudentExamSubmitRequest.AnswerItem item : req.getAnswers()) {
        if (item == null || item.getQuestionId() == null) continue;
        ExamQuestion q = qMap.get(item.getQuestionId());
        if (q == null) continue;
        String ans = item.getAnswer() == null ? "" : item.getAnswer();
        Double s = autoScore(q, ans);
        examMapper.upsertAnswer(attemptId, q.getId(), ans, s);
        if (s != null) objectiveScore += s;
      }
    }

    double totalScore = objectiveScore;
    int rows = examMapper.submitAttempt(attemptId, studentId, objectiveScore, totalScore);
    if (rows <= 0) throw new RuntimeException("提交失败，请重试");

    Map<String, Object> resp = new HashMap<>();
    resp.put("attemptId", attemptId);
    resp.put("objectiveScore", objectiveScore);
    resp.put("totalScore", totalScore);
    return resp;
  }

  private Double autoScore(ExamQuestion q, String studentAnswer) {
    if (q == null) return 0.0;
    Integer type = q.getQType();
    Double full = q.getScore() == null ? 0.0 : q.getScore();
    if (type == null) return 0.0;
    // 5 简答：不自动判分
    if (type == 5) return 0.0;

    String correct = q.getCorrectAnswer();
    if (correct == null) return 0.0;

    String a = normalizeAnswer(type, studentAnswer);
    String c = normalizeAnswer(type, correct);
    if (a.isEmpty() || c.isEmpty()) return 0.0;
    return a.equals(c) ? full : 0.0;
  }

  private String normalizeAnswer(Integer qType, String ans) {
    if (ans == null) return "";
    String s = ans.trim();
    if (s.isEmpty()) return "";
    // 单选/判断：统一大写
    if (qType == 1 || qType == 3) {
      return s.toUpperCase();
    }
    // 多选：拆分后排序去重，统一用逗号连接
    if (qType == 2) {
      String[] parts = s.toUpperCase().replace(" ", "").split("[,，;；/|]+");
      java.util.Set<String> set = new java.util.TreeSet<>();
      for (String p : parts) {
        if (p == null) continue;
        String t = p.trim();
        if (!t.isEmpty()) set.add(t);
      }
      return String.join(",", set);
    }
    // 填空：简单 trim 后比较（大小写敏感可自行调整）
    return s;
  }

  public List<com.edu.classroom.dto.student.StudentRecommendationDto> listRecommendations(Long studentId, Long courseId) {
    assertStudentInCourse(studentId, courseId);
    return recommendationMapper.listByStudentAndCourse(studentId, courseId);
  }

  public void readRecommendation(Long studentId, Long pushId) {
    if (pushId == null) throw new RuntimeException("推送记录ID不能为空");
    recommendationMapper.markRead(pushId, studentId);
  }

  private void assertStudentInCourse(Long studentId, Long courseId) {
    if (studentId == null || courseId == null) throw new RuntimeException("参数错误");
    int cnt = courseMemberMapper.countStudentInCourse(courseId, studentId);
    if (cnt <= 0) throw new RuntimeException("无权限或已移出课程");
  }
}
