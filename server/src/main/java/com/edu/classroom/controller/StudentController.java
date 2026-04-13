package com.edu.classroom.controller;

import com.edu.classroom.dto.exam.ExamPaperDetailResponse;
import com.edu.classroom.dto.exam.StudentExamSubmitRequest;
import com.edu.classroom.dto.student.StudentAssignmentSubmitRequest;
import com.edu.classroom.dto.student.StudentCheckinHistoryDto;
import com.edu.classroom.dto.student.StudentRecommendationDto;
import com.edu.classroom.entity.*;
import com.edu.classroom.service.StudentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
  private final StudentService studentService;

  public StudentController(StudentService studentService) {
    this.studentService = studentService;
  }

  @GetMapping("/courses")
  public ResponseEntity<List<Course>> myCourses(@RequestAttribute("uid") Long uid) {
    return ResponseEntity.ok(studentService.listJoinedCourses(uid));
  }

  @PostMapping("/join/code")
  public ResponseEntity<Course> joinByCode(@RequestAttribute("uid") Long uid, @RequestParam("courseCode") String courseCode) {
    return ResponseEntity.ok(studentService.joinByCode(uid, courseCode, "CODE"));
  }

  @GetMapping("/courses/{courseId}")
  public ResponseEntity<Course> courseDetail(@RequestAttribute("uid") Long uid, @PathVariable Long courseId) {
    return ResponseEntity.ok(studentService.getCourse(uid, courseId));
  }

  @GetMapping("/resources")
  public ResponseEntity<List<CourseResource>> resources(@RequestAttribute("uid") Long uid, @RequestParam Long courseId) {
    return ResponseEntity.ok(studentService.listResources(uid, courseId));
  }

  @PostMapping("/resources/progress")
  public ResponseEntity<Void> setProgress(@RequestAttribute("uid") Long uid, @RequestParam Long resourceId, @RequestParam Integer status, @RequestParam Integer percent) {
    studentService.setProgress(resourceId, uid, status, percent);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/assignments")
  public ResponseEntity<List<Assignment>> assignments(@RequestAttribute("uid") Long uid, @RequestParam Long courseId) {
    return ResponseEntity.ok(studentService.listAssignments(uid, courseId));
  }

  @GetMapping("/assignments/{assignmentId}")
  public ResponseEntity<Map<String, Object>> assignmentDetail(@RequestAttribute("uid") Long uid, @PathVariable Long assignmentId) {
    return ResponseEntity.ok(studentService.assignmentDetail(assignmentId, uid));
  }

  @PostMapping("/assignments/{assignmentId}/submit")
  public ResponseEntity<Void> submit(@RequestAttribute("uid") Long uid,
                                     @PathVariable Long assignmentId,
                                     @RequestBody(required = false) StudentAssignmentSubmitRequest req) {
    studentService.submitAssignment(assignmentId, uid, req);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/assignments/{assignmentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> uploadAssignmentFile(@RequestAttribute("uid") Long uid,
                                                                  @PathVariable Long assignmentId,
                                                                  @RequestParam("file") MultipartFile file,
                                                                  @RequestParam(value = "originalFileName", required = false) String originalFileName) {
    return ResponseEntity.ok(studentService.uploadAssignmentSubmissionFile(assignmentId, uid, file, originalFileName));
  }

  @PostMapping("/checkin/{checkinId}")
  public ResponseEntity<Void> checkin(@RequestAttribute("uid") Long uid, @PathVariable Long checkinId, @RequestParam(defaultValue = "CLICK") String source) {
    studentService.checkin(checkinId, uid, source);
    return ResponseEntity.ok().build();
  }

  /** 使用短签到码；路径不可为 /checkin/code，否则与 {checkinId} 冲突 */
  @PostMapping("/checkin/by-code")
  public ResponseEntity<Void> checkinByCode(@RequestAttribute("uid") Long uid,
                                            @RequestParam("code") String code,
                                            @RequestParam(defaultValue = "CLICK") String source) {
    studentService.checkinByCode(code, uid, source);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/checkins")
  public ResponseEntity<List<StudentCheckinHistoryDto>> checkinHistory(@RequestAttribute("uid") Long uid,
                                                                       @RequestParam Long courseId) {
    return ResponseEntity.ok(studentService.listCheckinHistory(uid, courseId));
  }

  @GetMapping("/grades/final")
  public ResponseEntity<Map<String, Object>> finalGrade(@RequestAttribute("uid") Long uid, @RequestParam Long courseId) {
    return ResponseEntity.ok(studentService.finalGradeDetail(courseId, uid));
  }

  // ===== 习题/试卷模块 =====

  @GetMapping("/exams")
  public ResponseEntity<List<ExamPaper>> listExams(@RequestAttribute("uid") Long uid, @RequestParam Long courseId) {
    return ResponseEntity.ok(studentService.listAvailableExamPapers(uid, courseId));
  }

  @GetMapping("/exams/{paperId}")
  public ResponseEntity<ExamPaperDetailResponse> examDetail(@RequestAttribute("uid") Long uid,
                                                            @PathVariable Long paperId,
                                                            @RequestParam Long courseId) {
    return ResponseEntity.ok(studentService.examPaperDetailForStudent(uid, courseId, paperId));
  }

  @PostMapping("/exams/{paperId}/start")
  public ResponseEntity<Map<String, Object>> startExam(@RequestAttribute("uid") Long uid,
                                                       @PathVariable Long paperId,
                                                       @RequestParam Long courseId) {
    Long attemptId = studentService.startExamAttempt(uid, courseId, paperId);
    Map<String, Object> body = new HashMap<>();
    body.put("attemptId", attemptId);
    return ResponseEntity.ok(body);
  }

  @PostMapping("/exams/{paperId}/attempts/{attemptId}/submit")
  public ResponseEntity<Map<String, Object>> submitExam(@RequestAttribute("uid") Long uid,
                                                        @PathVariable Long paperId,
                                                        @PathVariable Long attemptId,
                                                        @RequestParam Long courseId,
                                                        @RequestBody(required = false) StudentExamSubmitRequest req) {
    return ResponseEntity.ok(studentService.submitExamAttempt(uid, courseId, paperId, attemptId, req));
  }

  @GetMapping("/recommendations")
  public ResponseEntity<List<StudentRecommendationDto>> recommendations(@RequestAttribute("uid") Long uid,
                                                                        @RequestParam Long courseId) {
    return ResponseEntity.ok(studentService.listRecommendations(uid, courseId));
  }

  @PostMapping("/recommendations/{pushId}/read")
  public ResponseEntity<?> readRecommendation(@RequestAttribute("uid") Long uid,
                                              @PathVariable Long pushId) {
    studentService.readRecommendation(uid, pushId);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }
}
