package com.edu.classroom.mapper;

import com.edu.classroom.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {
  SysUser findByUsername(@Param("username") String username);
  SysUser findById(@Param("id") Long id);

  List<SysUser> list(@Param("keyword") String keyword,
                     @Param("roleCode") String roleCode,
                     @Param("status") Integer status,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

  long count(@Param("keyword") String keyword,
             @Param("roleCode") String roleCode,
             @Param("status") Integer status);

  Long generateId();
  int insert(SysUser user);
  int updateProfile(SysUser user);
  int updateRole(@Param("id") Long id, @Param("roleCode") String roleCode);
  int updateStatus(@Param("id") Long id, @Param("status") Integer status);
  int updatePasswordHash(@Param("id") Long id, @Param("passwordHash") String passwordHash);
  int softDelete(@Param("id") Long id);
}
