package com.edu.classroom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface LearningProgressMapper {
  Integer countCompletedResources(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

  Double avgProgressPercent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

  LocalDateTime lastActiveAt(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}

