package com.edu.classroom.mapper;

import com.edu.classroom.dto.student.StudentRecommendationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendationMapper {
  int insertPush(@Param("teacherId") Long teacherId,
                 @Param("courseId") Long courseId,
                 @Param("studentId") Long studentId,
                 @Param("resourceId") Long resourceId,
                 @Param("reason") String reason);

  List<StudentRecommendationDto> listByStudentAndCourse(@Param("studentId") Long studentId,
                                                         @Param("courseId") Long courseId);

  int markRead(@Param("pushId") Long pushId, @Param("studentId") Long studentId);
}

