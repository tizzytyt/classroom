package com.edu.classroom.mapper;

import com.edu.classroom.entity.CourseGradeRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseGradeRuleMapper {
  CourseGradeRule findByCourseId(@Param("courseId") Long courseId);

  int upsert(@Param("courseId") Long courseId,
             @Param("assignmentWeight") Double assignmentWeight,
             @Param("checkinWeight") Double checkinWeight,
             @Param("resourceWeight") Double resourceWeight,
             @Param("examWeight") Double examWeight);
}
