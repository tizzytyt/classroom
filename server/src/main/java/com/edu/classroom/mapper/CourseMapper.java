package com.edu.classroom.mapper;

import com.edu.classroom.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {
  List<Course> listJoined(@Param("studentId") Long studentId);
  List<Course> listTeaching(@Param("teacherId") Long teacherId);
  Course findById(@Param("id") Long id);
  Course findByIdAndTeacher(@Param("id") Long id, @Param("teacherId") Long teacherId);
  Course findByCode(@Param("code") String code);
  int upsertMember(@Param("courseId") Long courseId, @Param("userId") Long userId, @Param("joinType") String joinType);
  int upsertTeacherMember(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);
  int insertCourse(Course course);
  int updateCourseByTeacher(Course course);
  int softDeleteByTeacher(@Param("id") Long id, @Param("teacherId") Long teacherId);
}
