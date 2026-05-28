-- 为学生用户表增加紧急联络人字段
ALTER TABLE `sys_user`
  ADD COLUMN `emergency_contact_name` varchar(50) NULL DEFAULT NULL COMMENT '紧急联络人姓名' AFTER `avatar_url`,
  ADD COLUMN `emergency_contact_phone` varchar(20) NULL DEFAULT NULL COMMENT '紧急联络人电话' AFTER `emergency_contact_name`,
  ADD COLUMN `emergency_contact_relation` varchar(20) NULL DEFAULT NULL COMMENT '与本人关系' AFTER `emergency_contact_phone`;
