package com.edu.classroom.controller;

import com.edu.classroom.dto.teacher.TeacherCourseCreateRequest;
import com.edu.classroom.dto.teacher.TeacherResourceCreateRequest;
import com.edu.classroom.dto.teacher.TeacherAssignmentCreateRequest;
import com.edu.classroom.dto.teacher.TeacherGradeAssignmentRequest;
import com.edu.classroom.dto.teacher.TeacherResourceUpdateRequest;
import com.edu.classroom.dto.teacher.AssignmentSubmissionGradeDto;
import com.edu.classroom.dto.teacher.CheckinStatsResponse;
import com.edu.classroom.dto.teacher.CourseStatsOverviewResponse;
import com.edu.classroom.dto.teacher.HomeworkCompletionStatsResponse;
import com.edu.classroom.dto.teacher.ScoreDistributionResponse;
import com.edu.classroom.dto.teacher.TeacherStudentGradeItem;
import com.edu.classroom.dto.teacher.StudentLearningMonitorDto;
import com.edu.classroom.dto.teacher.TeacherPushRecommendationRequest;
import com.edu.classroom.dto.exam.TeacherExamPaperCreateRequest;
import com.edu.classroom.dto.exam.TeacherExamPaperWithQuestionsCreateRequest;
import com.edu.classroom.dto.exam.TeacherExamQuestionCreateRequest;
import com.edu.classroom.dto.exam.TeacherExamAttemptListItem;
import com.edu.classroom.dto.exam.TeacherExamAttemptGradingResponse;
import com.edu.classroom.dto.exam.TeacherExamGradeSaveRequest;
import com.edu.classroom.dto.exam.ExamPaperDetailResponse;
import com.edu.classroom.entity.Course;
import com.edu.classroom.entity.CourseResource;
import com.edu.classroom.entity.Assignment;
import com.edu.classroom.entity.Checkin;
import com.edu.classroom.entity.ExamPaper;
import com.edu.classroom.entity.ExamQuestion;
import com.edu.classroom.entity.SysUser;
import com.edu.classroom.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {
  private final TeacherService teacherService;

  public TeacherController(TeacherService teacherService) {
    this.teacherService = teacherService;
  }

  private boolean isTeacher(Object role) {
    return "TEACHER".equals(String.valueOf(role));
  }

  private ResponseEntity<Map<String, Object>> forbidden() {
    Map<String, Object> body = new HashMap<>();
    body.put("message", "无权限");
    return ResponseEntity.status(403).body(body);
  }

  @GetMapping("/courses")
  public ResponseEntity<?> myCourses(@RequestAttribute("uid") Long uid, @RequestAttribute("role") Object role) {
    if (!isTeacher(role)) return forbidden();
    List<Course> courses = teacherService.listTeachingCourses(uid);
    return ResponseEntity.ok(courses);
  }

  @GetMapping("/courses/{courseId}")
  public ResponseEntity<?> myCourseDetail(@RequestAttribute("uid") Long uid,
                                          @RequestAttribute("role") Object role,
                                          @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.getMyCourse(uid, courseId));
  }

  @PostMapping("/courses")
  public ResponseEntity<?> createCourse(@RequestAttribute("uid") Long uid,
                                        @RequestAttribute("role") Object role,
                                        @RequestBody TeacherCourseCreateRequest req) {
    if (!isTeacher(role)) return forbidden();
    Course created = teacherService.createCourse(uid, req);
    return ResponseEntity.ok(created);
  }

  @PutMapping("/courses/{courseId}")
  public ResponseEntity<?> updateCourse(@RequestAttribute("uid") Long uid,
                                        @RequestAttribute("role") Object role,
                                        @PathVariable Long courseId,
                                        @RequestBody TeacherCourseCreateRequest req) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.updateCourse(uid, courseId, req));
  }

  @DeleteMapping("/courses/{courseId}")
  public ResponseEntity<?> deleteCourse(@RequestAttribute("uid") Long uid,
                                        @RequestAttribute("role") Object role,
                                        @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    teacherService.deleteCourse(uid, courseId);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @GetMapping("/courses/{courseId}/students")
  public ResponseEntity<?> listCourseStudents(@RequestAttribute("uid") Long uid,
                                               @RequestAttribute("role") Object role,
                                               @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    List<SysUser> students = teacherService.listCourseStudents(uid, courseId);
    return ResponseEntity.ok(students);
  }

  @DeleteMapping("/courses/{courseId}/students/{studentId}")
  public ResponseEntity<?> removeCourseStudent(@RequestAttribute("uid") Long uid,
                                                 @RequestAttribute("role") Object role,
                                                 @PathVariable Long courseId,
                                                 @PathVariable Long studentId) {
    if (!isTeacher(role)) return forbidden();
    teacherService.removeCourseStudent(uid, courseId, studentId);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @GetMapping("/courses/{courseId}/resources")
  public ResponseEntity<?> listTeacherResources(@RequestAttribute("uid") Long uid,
                                                  @RequestAttribute("role") Object role,
                                                  @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    List<CourseResource> list = teacherService.listTeacherResources(uid, courseId);
    return ResponseEntity.ok(list);
  }

  @PostMapping("/courses/{courseId}/resources")
  public ResponseEntity<?> createResource(@RequestAttribute("uid") Long uid,
                                            @RequestAttribute("role") Object role,
                                            @PathVariable Long courseId,
                                            @RequestBody TeacherResourceCreateRequest req) {
    if (!isTeacher(role)) return forbidden();
    CourseResource created = teacherService.createResource(uid, courseId, req);
    return ResponseEntity.ok(created);
  }

  @PostMapping("/courses/{courseId}/resources/upload")
  public ResponseEntity<?> uploadResourceFile(@RequestAttribute("uid") Long uid,
                                               @RequestAttribute("role") Object role,
                                               @PathVariable Long courseId,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "originalFileName", required = false) String originalFileName) {
    if (!isTeacher(role)) return forbidden();
    CourseResource uploaded = teacherService.uploadResourceFile(uid, courseId, file, originalFileName);
    return ResponseEntity.ok(uploaded);
  }

  @PutMapping("/courses/{courseId}/resources/{resourceId}")
  public ResponseEntity<?> updateResource(@RequestAttribute("uid") Long uid,
                                           @RequestAttribute("role") Object role,
                                           @PathVariable Long courseId,
                                           @PathVariable Long resourceId,
                                           @RequestBody TeacherResourceUpdateRequest req) {
    if (!isTeacher(role)) return forbidden();
    CourseResource updated = teacherService.updateResource(uid, courseId, resourceId, req);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/courses/{courseId}/resources/{resourceId}")
  public ResponseEntity<?> deleteResource(@RequestAttribute("uid") Long uid,
                                           @RequestAttribute("role") Object role,
                                           @PathVariable Long courseId,
                                           @PathVariable Long resourceId) {
    if (!isTeacher(role)) return forbidden();
    teacherService.deleteResource(uid, courseId, resourceId);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @GetMapping("/courses/{courseId}/assignments")
  public ResponseEntity<?> listTeacherAssignments(@RequestAttribute("uid") Long uid,
                                                  @RequestAttribute("role") Object role,
                                                  @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    List<Assignment> list = teacherService.listTeacherAssignments(uid, courseId);
    return ResponseEntity.ok(list);
  }

  @PostMapping("/courses/{courseId}/assignments")
  public ResponseEntity<?> createAssignment(@RequestAttribute("uid") Long uid,
                                              @RequestAttribute("role") Object role,
                                              @PathVariable Long courseId,
                                              @RequestBody TeacherAssignmentCreateRequest req) {
    if (!isTeacher(role)) return forbidden();
    Assignment created = teacherService.createAssignment(uid, courseId, req);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/courses/{courseId}/assignments/{assignmentId}")
  public ResponseEntity<?> teacherAssignmentDetail(@RequestAttribute("uid") Long uid,
                                                      @RequestAttribute("role") Object role,
                                                      @PathVariable Long courseId,
                                                      @PathVariable Long assignmentId) {
    if (!isTeacher(role)) return forbidden();
    Assignment a = teacherService.getTeacherAssignment(uid, courseId, assignmentId);
    Map<String, Object> res = new HashMap<>();
    res.put("assignment", a);
    return ResponseEntity.ok(res);
  }

  @GetMapping("/courses/{courseId}/checkins")
  public ResponseEntity<?> listTeacherCheckins(@RequestAttribute("uid") Long uid,
                                               @RequestAttribute("role") Object role,
                                               @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    List<Checkin> list = teacherService.listTeacherCheckins(uid, courseId);
    return ResponseEntity.ok(list);
  }

  @PostMapping("/courses/{courseId}/checkins")
  public ResponseEntity<?> createCheckin(@RequestAttribute("uid") Long uid,
                                         @RequestAttribute("role") Object role,
                                         @PathVariable Long courseId,
                                         @RequestParam("title") String title,
                                         @RequestParam(value = "endAt", required = false) String endAt) {
    if (!isTeacher(role)) return forbidden();
    Checkin c = teacherService.createCheckin(uid, courseId, title, endAt);
    return ResponseEntity.ok(c);
  }

  @GetMapping("/courses/{courseId}/assignments/{assignmentId}/submissions")
  public ResponseEntity<?> listTeacherSubmissions(@RequestAttribute("uid") Long uid,
                                                    @RequestAttribute("role") Object role,
                                                    @PathVariable Long courseId,
                                                    @PathVariable Long assignmentId) {
    if (!isTeacher(role)) return forbidden();
    List<AssignmentSubmissionGradeDto> list = teacherService.listTeacherAssignmentSubmissions(uid, courseId, assignmentId);
    return ResponseEntity.ok(list);
  }

  @PutMapping("/courses/{courseId}/assignments/{assignmentId}/submissions/{submissionId}/grade")
  public ResponseEntity<?> gradeSubmission(@RequestAttribute("uid") Long uid,
                                              @RequestAttribute("role") Object role,
                                              @PathVariable Long courseId,
                                              @PathVariable Long assignmentId,
                                              @PathVariable Long submissionId,
                                              @RequestBody TeacherGradeAssignmentRequest req) {
    if (!isTeacher(role)) return forbidden();
    teacherService.gradeTeacherSubmission(uid, courseId, assignmentId, submissionId, req == null ? null : req.getScore(), req == null ? null : req.getComment());
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @PostMapping("/courses/{courseId}/checkins/{checkinId}/close")
  public ResponseEntity<?> closeCheckin(@RequestAttribute("uid") Long uid,
                                        @RequestAttribute("role") Object role,
                                        @PathVariable Long courseId,
                                        @PathVariable Long checkinId) {
    if (!isTeacher(role)) return forbidden();
    teacherService.closeCheckin(uid, courseId, checkinId);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @GetMapping("/courses/{courseId}/checkins/{checkinId}/stats")
  public ResponseEntity<?> checkinStats(@RequestAttribute("uid") Long uid,
                                        @RequestAttribute("role") Object role,
                                        @PathVariable Long courseId,
                                        @PathVariable Long checkinId) {
    if (!isTeacher(role)) return forbidden();
    CheckinStatsResponse resp = teacherService.checkinStats(uid, courseId, checkinId);
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/courses/{courseId}/stats/overview")
  public ResponseEntity<?> courseStatsOverview(@RequestAttribute("uid") Long uid,
                                               @RequestAttribute("role") Object role,
                                               @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    CourseStatsOverviewResponse resp = teacherService.courseStatsOverview(uid, courseId);
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/courses/{courseId}/grades/students")
  public ResponseEntity<?> listStudentGrades(@RequestAttribute("uid") Long uid,
                                            @RequestAttribute("role") Object role,
                                            @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    List<TeacherStudentGradeItem> list = teacherService.listStudentGradesForCourse(uid, courseId);
    return ResponseEntity.ok(list);
  }

  @GetMapping("/courses/{courseId}/stats/homeworks")
  public ResponseEntity<?> homeworkCompletionStats(@RequestAttribute("uid") Long uid,
                                                   @RequestAttribute("role") Object role,
                                                   @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    List<HomeworkCompletionStatsResponse> list = teacherService.homeworkCompletionStats(uid, courseId);
    return ResponseEntity.ok(list);
  }

  @GetMapping("/courses/{courseId}/stats/score-distribution")
  public ResponseEntity<?> scoreDistribution(@RequestAttribute("uid") Long uid,
                                             @RequestAttribute("role") Object role,
                                             @PathVariable Long courseId,
                                             @RequestParam(value = "homeworkId", required = false) Long homeworkId) {
    if (!isTeacher(role)) return forbidden();
    ScoreDistributionResponse resp = teacherService.courseScoreDistribution(uid, courseId, homeworkId);
    return ResponseEntity.ok(resp);
  }

  // ===== 习题/试卷模块 =====

  @PostMapping("/courses/{courseId}/exams")
  public ResponseEntity<?> createExamPaper(@RequestAttribute("uid") Long uid,
                                           @RequestAttribute("role") Object role,
                                           @PathVariable Long courseId,
                                           @RequestBody TeacherExamPaperCreateRequest req) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.createExamPaper(uid, courseId, req));
  }

  /**
   * 一次性创建试卷并写入题目（小程序端仅需一次提交）。
   */
  @PostMapping("/courses/{courseId}/exams/with-questions")
  public ResponseEntity<?> createExamPaperWithQuestions(@RequestAttribute("uid") Long uid,
                                                        @RequestAttribute("role") Object role,
                                                        @PathVariable Long courseId,
                                                        @RequestBody TeacherExamPaperWithQuestionsCreateRequest req) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.createExamPaperWithQuestions(uid, courseId, req));
  }

  @GetMapping("/courses/{courseId}/exams")
  public ResponseEntity<?> listExamPapers(@RequestAttribute("uid") Long uid,
                                          @RequestAttribute("role") Object role,
                                          @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.listExamPapers(uid, courseId));
  }

  @GetMapping("/courses/{courseId}/exams/pending-grading-counts")
  public ResponseEntity<?> pendingGradingCounts(@RequestAttribute("uid") Long uid,
                                                @RequestAttribute("role") Object role,
                                                @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.pendingGradingCounts(uid, courseId));
  }

  @GetMapping("/courses/{courseId}/exams/{paperId}")
  public ResponseEntity<?> examPaperDetail(@RequestAttribute("uid") Long uid,
                                           @RequestAttribute("role") Object role,
                                           @PathVariable Long courseId,
                                           @PathVariable Long paperId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.examPaperDetailForTeacher(uid, courseId, paperId));
  }

  @PostMapping("/courses/{courseId}/exams/{paperId}/questions")
  public ResponseEntity<?> addExamQuestion(@RequestAttribute("uid") Long uid,
                                           @RequestAttribute("role") Object role,
                                           @PathVariable Long courseId,
                                           @PathVariable Long paperId,
                                           @RequestBody TeacherExamQuestionCreateRequest req) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.addExamQuestion(uid, courseId, paperId, req));
  }

  @PostMapping("/courses/{courseId}/exams/{paperId}/publish")
  public ResponseEntity<?> publishExamPaper(@RequestAttribute("uid") Long uid,
                                            @RequestAttribute("role") Object role,
                                            @PathVariable Long courseId,
                                            @PathVariable Long paperId) {
    if (!isTeacher(role)) return forbidden();
    teacherService.publishExamPaper(uid, courseId, paperId);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @GetMapping("/courses/{courseId}/exams/{paperId}/attempts")
  public ResponseEntity<?> listExamAttempts(@RequestAttribute("uid") Long uid,
                                           @RequestAttribute("role") Object role,
                                           @PathVariable Long courseId,
                                           @PathVariable Long paperId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.listExamAttemptsForGrading(uid, courseId, paperId));
  }

  @GetMapping("/courses/{courseId}/exams/{paperId}/analysis")
  public ResponseEntity<?> examAnalysis(@RequestAttribute("uid") Long uid,
                                       @RequestAttribute("role") Object role,
                                       @PathVariable Long courseId,
                                       @PathVariable Long paperId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.examPaperAnalysis(uid, courseId, paperId));
  }

  @GetMapping("/courses/{courseId}/exams/{paperId}/export-scores")
  public ResponseEntity<byte[]> exportScores(@RequestAttribute("uid") Long uid,
                                             @RequestAttribute("role") Object role,
                                             @PathVariable Long courseId,
                                             @PathVariable Long paperId) {
    if (!isTeacher(role)) return ResponseEntity.status(403).build();
    byte[] bytes = teacherService.exportExamScoresExcel(uid, courseId, paperId);
    String fileName = "exam_scores_" + paperId + ".xls";
    try {
      String enc = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + enc)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
          .body(bytes);
    } catch (Exception e) {
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scores.xls\"")
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
          .body(bytes);
    }
  }

  @GetMapping("/courses/{courseId}/grades/export")
  public ResponseEntity<byte[]> exportCourseFinalScores(@RequestAttribute("uid") Long uid,
                                                        @RequestAttribute("role") Object role,
                                                        @PathVariable Long courseId) {
    if (!isTeacher(role)) return ResponseEntity.status(403).build();
    byte[] bytes = teacherService.exportCourseFinalScoresExcel(uid, courseId);
    String fileName = "course_final_scores_" + courseId + ".xls";
    try {
      String enc = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + enc)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
          .body(bytes);
    } catch (Exception e) {
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"course_final_scores.xls\"")
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
          .body(bytes);
    }
  }

  @GetMapping("/courses/{courseId}/exams/{paperId}/attempts/{attemptId}")
  public ResponseEntity<?> examAttemptGradingDetail(@RequestAttribute("uid") Long uid,
                                                   @RequestAttribute("role") Object role,
                                                   @PathVariable Long courseId,
                                                   @PathVariable Long paperId,
                                                   @PathVariable Long attemptId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.getExamAttemptForGrading(uid, courseId, paperId, attemptId));
  }

  @PutMapping("/courses/{courseId}/exams/{paperId}/attempts/{attemptId}/grades")
  public ResponseEntity<?> saveExamAttemptGrades(@RequestAttribute("uid") Long uid,
                                                @RequestAttribute("role") Object role,
                                                @PathVariable Long courseId,
                                                @PathVariable Long paperId,
                                                @PathVariable Long attemptId,
                                                @RequestBody(required = false) TeacherExamGradeSaveRequest req) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.saveExamAttemptGrades(uid, courseId, paperId, attemptId, req));
  }

  // ===== 学习监控 & 动态推荐 =====

  @GetMapping("/courses/{courseId}/monitor/students")
  public ResponseEntity<?> monitorStudents(@RequestAttribute("uid") Long uid,
                                           @RequestAttribute("role") Object role,
                                           @PathVariable Long courseId) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.monitorStudents(uid, courseId));
  }

  @GetMapping("/courses/{courseId}/monitor/students/{studentId}/recommendations")
  public ResponseEntity<?> recommendResources(@RequestAttribute("uid") Long uid,
                                              @RequestAttribute("role") Object role,
                                              @PathVariable Long courseId,
                                              @PathVariable Long studentId,
                                              @RequestParam(value = "limit", required = false) Integer limit) {
    if (!isTeacher(role)) return forbidden();
    return ResponseEntity.ok(teacherService.recommendResourcesForStudent(uid, courseId, studentId, limit));
  }

  @PostMapping("/courses/{courseId}/monitor/students/{studentId}/recommendations/push")
  public ResponseEntity<?> pushRecommendations(@RequestAttribute("uid") Long uid,
                                               @RequestAttribute("role") Object role,
                                               @PathVariable Long courseId,
                                               @PathVariable Long studentId,
                                               @RequestBody TeacherPushRecommendationRequest req) {
    if (!isTeacher(role)) return forbidden();
    int pushed = teacherService.pushRecommendations(uid, courseId, studentId, req == null ? null : req.getResourceIds(), req == null ? null : req.getReason());
    Map<String, Object> body = new HashMap<>();
    body.put("pushedCount", pushed);
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }
}
