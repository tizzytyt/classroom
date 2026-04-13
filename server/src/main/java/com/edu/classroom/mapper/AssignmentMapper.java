package com.edu.classroom.mapper;

import com.edu.classroom.entity.Assignment;
import com.edu.classroom.entity.AssignmentSubmission;
import com.edu.classroom.entity.AssignmentGrade;
import com.edu.classroom.dto.teacher.AssignmentSubmissionGradeDto;
import com.edu.classroom.dto.teacher.HomeworkCompletionStatsResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssignmentMapper {
  List<Assignment> listByCourse(@Param("courseId") Long courseId);
  /** 学生端：作业列表附带本人已评分数（无提交或未批改则为 null） */
  List<Assignment> listByCourseForStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
  List<Assignment> listByCourseAndTeacher(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
  Assignment findById(@Param("id") Long id);
  Assignment findByIdAndTeacher(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId, @Param("id") Long id);
  AssignmentSubmission findSubmission(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId);
  int upsertSubmissionText(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId, @Param("text") String text, @Param("isLate") Integer isLate, @Param("attachmentUrls") String attachmentUrls);
  AssignmentGrade findGradeBySubmission(@Param("submissionId") Long submissionId);

  int insertAssignment(@Param("courseId") Long courseId,
                        @Param("title") String title,
                        @Param("content") String content,
                        @Param("dueAt") java.time.LocalDateTime dueAt,
                        @Param("totalScore") Double totalScore,
                        @Param("creatorId") Long creatorId,
                        @Param("attachmentUrl") String attachmentUrl);

  List<AssignmentSubmissionGradeDto> listSubmissionsForTeacher(@Param("teacherId") Long teacherId,
                                                                  @Param("courseId") Long courseId,
                                                                  @Param("assignmentId") Long assignmentId);

  int insertGradeByTeacher(@Param("teacherId") Long teacherId,
                            @Param("courseId") Long courseId,
                            @Param("assignmentId") Long assignmentId,
                            @Param("submissionId") Long submissionId,
                            @Param("score") Double score,
                            @Param("comment") String comment);

  Integer countAssignmentsByCourse(@Param("courseId") Long courseId);

  Integer countDistinctSubmitStudentsByCourse(@Param("courseId") Long courseId);

  Integer countDistinctSubmitStudentsByHomework(@Param("homeworkId") Long homeworkId);

  List<HomeworkCompletionStatsResponse> listHomeworkCompletionStats(@Param("teacherId") Long teacherId,
                                                                         @Param("courseId") Long courseId);

  List<Double> listAssignmentScoresForTeacher(@Param("teacherId") Long teacherId,
                                               @Param("courseId") Long courseId,
                                               @Param("homeworkId") Long homeworkId);

  Integer countSubmittedAssignmentsByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}
