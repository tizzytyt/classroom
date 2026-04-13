package com.edu.classroom.mapper;

import com.edu.classroom.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMemberMapper {
  List<SysUser> listCourseStudents(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

  int softRemoveCourseStudent(@Param("teacherId") Long teacherId,
                                @Param("courseId") Long courseId,
                                @Param("studentId") Long studentId);

  int countStudentInCourse(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

  java.util.List<SysUser> listCourseStudentsRaw(@Param("courseId") Long courseId);
}

