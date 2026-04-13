package com.edu.classroom.mapper;

import com.edu.classroom.entity.Checkin;
import com.edu.classroom.entity.CheckinRecord;
import com.edu.classroom.dto.student.StudentCheckinHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CheckinMapper {
  Checkin findById(@Param("id") Long id);

  Checkin findByCheckinCode(@Param("code") String code);

  Checkin findActiveByCheckinCode(@Param("code") String code);

  int countByCheckinCode(@Param("code") String code);
  CheckinRecord findRecord(@Param("checkinId") Long checkinId, @Param("studentId") Long studentId);
  int insertRecord(@Param("checkinId") Long checkinId, @Param("studentId") Long studentId, @Param("source") String source);
  int insertCheckin(@Param("courseId") Long courseId,
                    @Param("title") String title,
                    @Param("checkinCode") String checkinCode,
                    @Param("startAt") java.time.LocalDateTime startAt,
                    @Param("endAt") java.time.LocalDateTime endAt,
                    @Param("createdBy") Long createdBy);
  java.util.List<Checkin> listByCourseAndTeacher(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

  int closeByTeacher(@Param("teacherId") Long teacherId,
                     @Param("courseId") Long courseId,
                     @Param("checkinId") Long checkinId);

  java.util.List<CheckinRecord> listRecordsByCheckin(@Param("checkinId") Long checkinId);

   Integer countCheckinsByCourse(@Param("courseId") Long courseId);

   Integer countRecordsByCourse(@Param("courseId") Long courseId);

   Integer countRecordsByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

  java.util.List<StudentCheckinHistoryDto> listHistoryByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}
