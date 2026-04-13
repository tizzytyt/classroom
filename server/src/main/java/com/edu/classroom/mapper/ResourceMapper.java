package com.edu.classroom.mapper;

import com.edu.classroom.entity.CourseResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceMapper {
  List<CourseResource> listByCourse(@Param("courseId") Long courseId);
  List<CourseResource> listByCourseAndTeacher(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
  Long findCourseIdByResourceId(@Param("resourceId") Long resourceId);
  Integer countResourcesByCourse(@Param("courseId") Long courseId);
  List<CourseResource> listRecommendedForStudent(@Param("courseId") Long courseId,
                                                 @Param("studentId") Long studentId,
                                                 @Param("limit") Integer limit);
  int upsertProgress(@Param("resourceId") Long resourceId, @Param("userId") Long userId, @Param("status") Integer status, @Param("percent") Integer percent);
  int insertResource(@Param("courseId") Long courseId,
                      @Param("uploaderId") Long uploaderId,
                      @Param("title") String title,
                      @Param("description") String description,
                      @Param("category") String category,
                      @Param("fileUrl") String fileUrl,
                      @Param("fileName") String fileName,
                      @Param("fileSize") Long fileSize,
                      @Param("fileType") String fileType);

  int updateResourceByTeacher(@Param("teacherId") Long teacherId,
                               @Param("courseId") Long courseId,
                               @Param("resourceId") Long resourceId,
                               @Param("title") String title,
                               @Param("description") String description,
                               @Param("category") String category);

  int softDeleteResourceByTeacher(@Param("teacherId") Long teacherId,
                                   @Param("courseId") Long courseId,
                                   @Param("resourceId") Long resourceId);
}
