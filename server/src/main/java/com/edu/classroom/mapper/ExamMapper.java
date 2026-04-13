package com.edu.classroom.mapper;

import com.edu.classroom.dto.exam.ExamPaperDetailResponse;
import com.edu.classroom.dto.exam.TeacherExamAttemptListItem;
import com.edu.classroom.dto.exam.TeacherExamGradingQuestionDto;
import com.edu.classroom.dto.exam.TeacherExamQuestionAnalysisDto;
import com.edu.classroom.entity.ExamAttempt;
import com.edu.classroom.entity.ExamPaper;
import com.edu.classroom.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamMapper {
  Long genId();

  // teacher
  int insertPaper(@Param("id") Long id,
                  @Param("courseId") Long courseId,
                  @Param("title") String title,
                  @Param("durationMinutes") Integer durationMinutes,
                  @Param("startAt") java.time.LocalDateTime startAt,
                  @Param("endAt") java.time.LocalDateTime endAt,
                  @Param("shuffleQuestions") Integer shuffleQuestions,
                  @Param("creatorId") Long creatorId);

  List<ExamPaper> listTeacherPapers(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

  ExamPaper findPaperByTeacher(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId, @Param("paperId") Long paperId);

  int publishPaper(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId, @Param("paperId") Long paperId);

  int insertQuestion(@Param("id") Long id,
                     @Param("paperId") Long paperId,
                     @Param("qType") Integer qType,
                     @Param("stem") String stem,
                     @Param("score") Double score,
                     @Param("sortNo") Integer sortNo,
                     @Param("correctAnswer") String correctAnswer);

  int insertOption(@Param("id") Long id,
                   @Param("questionId") Long questionId,
                   @Param("optKey") String optKey,
                   @Param("optText") String optText,
                   @Param("sortNo") Integer sortNo);

  List<ExamQuestion> listQuestionsByPaper(@Param("paperId") Long paperId);

  List<ExamPaperDetailResponse.Option> listOptionsByPaper(@Param("paperId") Long paperId);

  // student
  List<ExamPaper> listStudentAvailablePapers(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

  ExamPaper findPaperForStudent(@Param("studentId") Long studentId, @Param("courseId") Long courseId, @Param("paperId") Long paperId);

  int insertAttempt(@Param("id") Long id, @Param("paperId") Long paperId, @Param("studentId") Long studentId);

  ExamAttempt findAttempt(@Param("attemptId") Long attemptId, @Param("studentId") Long studentId);

  int upsertAnswer(@Param("attemptId") Long attemptId,
                   @Param("questionId") Long questionId,
                   @Param("answer") String answer,
                   @Param("score") Double score);

  int submitAttempt(@Param("attemptId") Long attemptId,
                    @Param("studentId") Long studentId,
                    @Param("objectiveScore") Double objectiveScore,
                    @Param("totalScore") Double totalScore);

  List<ExamQuestion> listQuestionsWithCorrectByPaper(@Param("paperId") Long paperId);

  // teacher grading
  List<TeacherExamAttemptListItem> listSubmittedAttemptsForTeacher(@Param("teacherId") Long teacherId,
                                                                   @Param("courseId") Long courseId,
                                                                   @Param("paperId") Long paperId);

  ExamAttempt findAttemptForTeacher(@Param("teacherId") Long teacherId,
                                     @Param("courseId") Long courseId,
                                     @Param("paperId") Long paperId,
                                     @Param("attemptId") Long attemptId);

  List<TeacherExamGradingQuestionDto> listGradingQuestionsForAttempt(@Param("paperId") Long paperId,
                                                                     @Param("attemptId") Long attemptId);

  String findAnswerText(@Param("attemptId") Long attemptId, @Param("questionId") Long questionId);

  int updateAnswerScoreForTeacher(@Param("teacherId") Long teacherId,
                                  @Param("courseId") Long courseId,
                                  @Param("paperId") Long paperId,
                                  @Param("attemptId") Long attemptId,
                                  @Param("questionId") Long questionId,
                                  @Param("score") Double score);

  Double sumAnswerScores(@Param("attemptId") Long attemptId);

  Double sumObjectiveAnswerScores(@Param("attemptId") Long attemptId);

  int updateAttemptTotalScores(@Param("teacherId") Long teacherId,
                               @Param("courseId") Long courseId,
                               @Param("paperId") Long paperId,
                               @Param("attemptId") Long attemptId,
                               @Param("objectiveScore") Double objectiveScore,
                               @Param("totalScore") Double totalScore);

  // analysis
  java.util.List<TeacherExamQuestionAnalysisDto> listQuestionAnalysisByPaper(@Param("paperId") Long paperId);

  Integer countPublishedPapersByCourse(@Param("courseId") Long courseId);

  Integer countCompletedExamPapersByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

  Double avgBestExamScoreByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

  Double bestSubmittedScoreByPaperAndStudent(@Param("paperId") Long paperId, @Param("studentId") Long studentId);

  Integer countPendingSubjectiveAttemptsByPaper(@Param("teacherId") Long teacherId,
                                                @Param("courseId") Long courseId,
                                                @Param("paperId") Long paperId);
}

