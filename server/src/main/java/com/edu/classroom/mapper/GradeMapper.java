package com.edu.classroom.mapper;

import com.edu.classroom.dto.teacher.TeacherStudentGradeItem;
import com.edu.classroom.entity.CourseFinalGrade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GradeMapper {
  CourseFinalGrade findFinal(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

  List<Double> listFinalScoresForCourse(@Param("teacherId") Long teacherId,
                                          @Param("courseId") Long courseId);

  List<TeacherStudentGradeItem> listStudentGradesForCourse(@Param("teacherId") Long teacherId,
                                                            @Param("courseId") Long courseId);
}
