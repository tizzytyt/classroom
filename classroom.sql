/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : classroom

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 12/04/2026 17:02:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for assignment
-- ----------------------------
DROP TABLE IF EXISTS `assignment`;
CREATE TABLE `assignment`  (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `due_at` datetime NOT NULL,
  `total_score` decimal(6, 2) NOT NULL DEFAULT 100.00,
  `creator_id` bigint NOT NULL,
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `published_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_course_due`(`course_id` ASC, `due_at` ASC) USING BTREE,
  INDEX `idx_assignment_creator`(`creator_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for assignment_grade
-- ----------------------------
DROP TABLE IF EXISTS `assignment_grade`;
CREATE TABLE `assignment_grade`  (
  `id` bigint NOT NULL,
  `submission_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `score` decimal(6, 2) NOT NULL,
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `graded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_grade_submission`(`submission_id` ASC) USING BTREE,
  INDEX `idx_grade_teacher`(`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for assignment_submission
-- ----------------------------
DROP TABLE IF EXISTS `assignment_submission`;
CREATE TABLE `assignment_submission`  (
  `id` bigint NOT NULL,
  `assignment_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `submit_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `attachment_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件URL列表JSON',
  `submitted_at` datetime NULL DEFAULT NULL,
  `submit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `is_late` tinyint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 1,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submission_assignment_student`(`assignment_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_submission_assignment_status`(`assignment_id` ASC, `submit_status` ASC) USING BTREE,
  INDEX `idx_submission_student`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for checkin
-- ----------------------------
DROP TABLE IF EXISTS `checkin`;
CREATE TABLE `checkin`  (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `checkin_code` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生端输入的短签到码',
  `start_at` datetime NOT NULL,
  `end_at` datetime NULL DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_checkin_code`(`checkin_code` ASC) USING BTREE,
  INDEX `idx_checkin_course_time`(`course_id` ASC, `start_at` ASC) USING BTREE,
  INDEX `idx_checkin_creator`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for checkin_record
-- ----------------------------
DROP TABLE IF EXISTS `checkin_record`;
CREATE TABLE `checkin_record`  (
  `id` bigint NOT NULL,
  `checkin_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `checked_in_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_checkin_record`(`checkin_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_checkin_record_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_checkin_record_checkin`(`checkin_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `intro` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `teacher_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_code`(`course_code` ASC) USING BTREE,
  INDEX `idx_course_teacher_status`(`teacher_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_course_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_final_grade
-- ----------------------------
DROP TABLE IF EXISTS `course_final_grade`;
CREATE TABLE `course_final_grade`  (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `assignment_score` decimal(6, 2) NOT NULL DEFAULT 0.00,
  `checkin_score` decimal(6, 2) NOT NULL DEFAULT 0.00,
  `resource_score` decimal(6, 2) NOT NULL DEFAULT 0.00,
  `exam_score` decimal(6, 2) NOT NULL DEFAULT 0.00,
  `final_score` decimal(6, 2) NOT NULL DEFAULT 0.00,
  `calculated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_final_grade_course_student`(`course_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_final_grade_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_final_grade_course`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_grade_rule
-- ----------------------------
DROP TABLE IF EXISTS `course_grade_rule`;
CREATE TABLE `course_grade_rule`  (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `assignment_weight` decimal(5, 2) NOT NULL DEFAULT 0.70,
  `checkin_weight` decimal(5, 2) NOT NULL DEFAULT 0.20,
  `resource_weight` decimal(5, 2) NOT NULL DEFAULT 0.10,
  `exam_weight` decimal(5, 4) NOT NULL DEFAULT 0.0000,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_grade_rule_course`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_member
-- ----------------------------
DROP TABLE IF EXISTS `course_member`;
CREATE TABLE `course_member`  (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `member_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STUDENT',
  `join_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_member`(`course_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_course_member_user`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_course_member_course`(`course_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_resource
-- ----------------------------
DROP TABLE IF EXISTS `course_resource`;
CREATE TABLE `course_resource`  (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` bigint NOT NULL DEFAULT 0,
  `file_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `uploader_id` bigint NOT NULL,
  `published_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource_course_published`(`course_id` ASC, `published_at` ASC) USING BTREE,
  INDEX `idx_resource_uploader`(`uploader_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exam_answer
-- ----------------------------
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer`  (
  `id` bigint NOT NULL,
  `attempt_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `score` decimal(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_attempt_question`(`attempt_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_exam_answer_attempt`(`attempt_id` ASC) USING BTREE,
  INDEX `idx_exam_answer_question`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exam_attempt
-- ----------------------------
DROP TABLE IF EXISTS `exam_attempt`;
CREATE TABLE `exam_attempt`  (
  `id` bigint NOT NULL,
  `paper_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `started_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `submitted_at` datetime NULL DEFAULT NULL,
  `objective_score` decimal(10, 2) NULL DEFAULT NULL,
  `total_score` decimal(10, 2) NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1进行中 2已提交',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_attempt_paper`(`paper_id` ASC) USING BTREE,
  INDEX `idx_exam_attempt_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_exam_attempt_status`(`status` ASC) USING BTREE,
  INDEX `idx_exam_attempt_started`(`started_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exam_option
-- ----------------------------
DROP TABLE IF EXISTS `exam_option`;
CREATE TABLE `exam_option`  (
  `id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `opt_key` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `opt_text` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1有效 0删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_option_question`(`question_id` ASC) USING BTREE,
  INDEX `idx_exam_option_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exam_paper
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper`  (
  `id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `duration_minutes` int NULL DEFAULT NULL,
  `start_at` datetime NULL DEFAULT NULL,
  `end_at` datetime NULL DEFAULT NULL,
  `shuffle_questions` tinyint NOT NULL DEFAULT 0 COMMENT '是否打乱题目顺序：0否 1是',
  `creator_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已下线',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_paper_course`(`course_id` ASC) USING BTREE,
  INDEX `idx_exam_paper_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_exam_paper_status`(`status` ASC) USING BTREE,
  INDEX `idx_exam_paper_time`(`start_at` ASC, `end_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for exam_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question`  (
  `id` bigint NOT NULL,
  `paper_id` bigint NOT NULL,
  `q_type` tinyint NOT NULL COMMENT '1单选 2多选 3判断 4填空 5简答',
  `stem` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `sort_no` int NOT NULL DEFAULT 0,
  `correct_answer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1有效 0删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_question_paper`(`paper_id` ASC) USING BTREE,
  INDEX `idx_exam_question_type`(`q_type` ASC) USING BTREE,
  INDEX `idx_exam_question_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_progress
-- ----------------------------
DROP TABLE IF EXISTS `learning_progress`;
CREATE TABLE `learning_progress`  (
  `id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `learn_status` tinyint NOT NULL DEFAULT 0,
  `progress_percent` tinyint NOT NULL DEFAULT 0,
  `last_viewed_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_progress_resource_user`(`resource_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_progress_user`(`user_id` ASC, `learn_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for recommendation_push
-- ----------------------------
DROP TABLE IF EXISTS `recommendation_push`;
CREATE TABLE `recommendation_push`  (
  `id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `read_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rec_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_rec_course_student`(`course_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_rec_resource`(`resource_id` ASC) USING BTREE,
  INDEX `idx_rec_status`(`status` ASC) USING BTREE,
  INDEX `idx_rec_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for submission_file
-- ----------------------------
DROP TABLE IF EXISTS `submission_file`;
CREATE TABLE `submission_file`  (
  `id` bigint NOT NULL,
  `submission_id` bigint NOT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` bigint NOT NULL DEFAULT 0,
  `file_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_file_submission`(`submission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username` ASC) USING BTREE,
  INDEX `idx_sys_user_role_status`(`role_code` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试数据（登录密码均为 123456，与 AuthService 明文校验一致）
-- ----------------------------

INSERT INTO `sys_user` VALUES (9001, 'admin', '123456', '系统管理员', 'ADMIN', NULL, NULL, NULL, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00');
INSERT INTO `sys_user` VALUES (1001, 't_zhang', '123456', '张老师', 'TEACHER', '13800001001', 'zhang@edu.cn', NULL, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00');
INSERT INTO `sys_user` VALUES (1002, 't_li', '123456', '李老师', 'TEACHER', '13800001002', 'li@edu.cn', NULL, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00');
INSERT INTO `sys_user` VALUES (2001, 'stu_zhou', '123456', '周同学', 'STUDENT', '13900002001', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2002, 'stu_wu', '123456', '吴同学', 'STUDENT', '13900002002', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2003, 'stu_zheng', '123456', '郑同学', 'STUDENT', '13900002003', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2004, 'stu_wang', '123456', '王同学', 'STUDENT', '13900002004', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2005, 'stu_feng', '123456', '冯同学', 'STUDENT', '13900002005', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2006, 'stu_chen', '123456', '陈同学', 'STUDENT', '13900002006', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2007, 'stu_chu', '123456', '褚同学', 'STUDENT', '13900002007', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2008, 'stu_wei', '123456', '卫同学', 'STUDENT', '13900002008', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2009, 'stu_jiang', '123456', '蒋同学', 'STUDENT', '13900002009', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2010, 'stu_shen', '123456', '沈同学', 'STUDENT', '13900002010', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2011, 'stu_han', '123456', '韩同学', 'STUDENT', '13900002011', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2012, 'stu_yang', '123456', '杨同学', 'STUDENT', '13900002012', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2013, 'stu_zhu', '123456', '朱同学', 'STUDENT', '13900002013', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2014, 'stu_qin', '123456', '秦同学', 'STUDENT', '13900002014', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');
INSERT INTO `sys_user` VALUES (2015, 'stu_you', '123456', '尤同学', 'STUDENT', '13900002015', NULL, NULL, 1, '2026-03-02 09:00:00', '2026-03-02 09:00:00');

INSERT INTO `course` VALUES (3011, '数据库系统原理', 'C20263011', '关系模型、SQL、事务与并发控制', NULL, '计算机', 1001, 1, '2026-03-05 09:00:00', '2026-03-05 09:00:00', NULL);
INSERT INTO `course` VALUES (3012, 'Web 前端开发', 'C20263012', 'HTML/CSS/JavaScript 与小程序基础', NULL, '计算机', 1001, 1, '2026-03-05 09:30:00', '2026-03-05 09:30:00', NULL);
INSERT INTO `course` VALUES (3013, 'Java 程序设计', 'C20263013', '面向对象与集合框架', NULL, '计算机', 1002, 1, '2026-03-06 10:00:00', '2026-03-06 10:00:00', NULL);
INSERT INTO `course` VALUES (3014, '计算机网络', 'C20263014', 'TCP/IP、HTTP 与应用层协议', NULL, '计算机', 1002, 1, '2026-03-06 10:30:00', '2026-03-06 10:30:00', NULL);
INSERT INTO `course` VALUES (3015, '操作系统', 'C20263015', '进程、内存与文件系统', NULL, '计算机', 1001, 1, '2026-03-07 08:00:00', '2026-03-07 08:00:00', NULL);
INSERT INTO `course` VALUES (3016, '软件工程', 'C20263016', '需求、设计与测试', NULL, '计算机', 1002, 1, '2026-03-07 09:00:00', '2026-03-07 09:00:00', NULL);
INSERT INTO `course` VALUES (3017, 'Python 程序设计', 'C20263017', '语法、数据分析入门', NULL, '计算机', 1001, 1, '2026-03-08 14:00:00', '2026-03-08 14:00:00', NULL);
INSERT INTO `course` VALUES (3018, '数据结构与算法', 'C20263018', '线性表、树、图与排序', NULL, '计算机', 1002, 1, '2026-03-08 15:00:00', '2026-03-08 15:00:00', NULL);

INSERT INTO `course_grade_rule` VALUES (8111, 3011, 0.55, 0.15, 0.10, 0.2000, '2026-03-10 12:00:00');
INSERT INTO `course_grade_rule` VALUES (8112, 3012, 0.60, 0.15, 0.10, 0.1500, '2026-03-10 12:00:00');
INSERT INTO `course_grade_rule` VALUES (8113, 3013, 0.50, 0.20, 0.10, 0.2000, '2026-03-10 12:00:00');
INSERT INTO `course_grade_rule` VALUES (8114, 3014, 0.65, 0.15, 0.10, 0.1000, '2026-03-10 12:00:00');
INSERT INTO `course_grade_rule` VALUES (8115, 3015, 0.70, 0.15, 0.10, 0.0500, '2026-03-10 12:00:00');
INSERT INTO `course_grade_rule` VALUES (8116, 3016, 0.70, 0.20, 0.10, 0.0000, '2026-03-10 12:00:00');
INSERT INTO `course_grade_rule` VALUES (8117, 3017, 0.55, 0.20, 0.15, 0.1000, '2026-03-10 12:00:00');
INSERT INTO `course_grade_rule` VALUES (8118, 3018, 0.60, 0.15, 0.15, 0.1000, '2026-03-10 12:00:00');

INSERT INTO `course_member` VALUES (91001, 3011, 1001, 'TEACHER', 'CREATE', '2026-03-05 09:00:00', 1);
INSERT INTO `course_member` VALUES (91002, 3011, 2001, 'STUDENT', 'CODE', '2026-03-06 10:00:00', 1);
INSERT INTO `course_member` VALUES (91003, 3011, 2002, 'STUDENT', 'CODE', '2026-03-06 10:05:00', 1);
INSERT INTO `course_member` VALUES (91004, 3011, 2003, 'STUDENT', 'CODE', '2026-03-06 10:10:00', 1);
INSERT INTO `course_member` VALUES (91005, 3011, 2004, 'STUDENT', 'CODE', '2026-03-06 10:15:00', 1);
INSERT INTO `course_member` VALUES (91006, 3011, 2005, 'STUDENT', 'CODE', '2026-03-06 10:20:00', 1);
INSERT INTO `course_member` VALUES (91011, 3012, 1001, 'TEACHER', 'CREATE', '2026-03-05 09:30:00', 1);
INSERT INTO `course_member` VALUES (91012, 3012, 2002, 'STUDENT', 'CODE', '2026-03-06 11:00:00', 1);
INSERT INTO `course_member` VALUES (91013, 3012, 2003, 'STUDENT', 'CODE', '2026-03-06 11:05:00', 1);
INSERT INTO `course_member` VALUES (91014, 3012, 2006, 'STUDENT', 'CODE', '2026-03-06 11:10:00', 1);
INSERT INTO `course_member` VALUES (91015, 3012, 2007, 'STUDENT', 'CODE', '2026-03-06 11:15:00', 1);
INSERT INTO `course_member` VALUES (91016, 3012, 2008, 'STUDENT', 'CODE', '2026-03-06 11:20:00', 1);
INSERT INTO `course_member` VALUES (91021, 3013, 1002, 'TEACHER', 'CREATE', '2026-03-06 10:00:00', 1);
INSERT INTO `course_member` VALUES (91022, 3013, 2004, 'STUDENT', 'CODE', '2026-03-07 09:00:00', 1);
INSERT INTO `course_member` VALUES (91023, 3013, 2005, 'STUDENT', 'CODE', '2026-03-07 09:05:00', 1);
INSERT INTO `course_member` VALUES (91024, 3013, 2009, 'STUDENT', 'CODE', '2026-03-07 09:10:00', 1);
INSERT INTO `course_member` VALUES (91025, 3013, 2010, 'STUDENT', 'CODE', '2026-03-07 09:15:00', 1);
INSERT INTO `course_member` VALUES (91031, 3014, 1002, 'TEACHER', 'CREATE', '2026-03-06 10:30:00', 1);
INSERT INTO `course_member` VALUES (91032, 3014, 2001, 'STUDENT', 'CODE', '2026-03-07 10:00:00', 1);
INSERT INTO `course_member` VALUES (91033, 3014, 2006, 'STUDENT', 'CODE', '2026-03-07 10:05:00', 1);
INSERT INTO `course_member` VALUES (91034, 3014, 2011, 'STUDENT', 'CODE', '2026-03-07 10:10:00', 1);
INSERT INTO `course_member` VALUES (91035, 3014, 2012, 'STUDENT', 'CODE', '2026-03-07 10:15:00', 1);
INSERT INTO `course_member` VALUES (91041, 3015, 1001, 'TEACHER', 'CREATE', '2026-03-07 08:00:00', 1);
INSERT INTO `course_member` VALUES (91042, 3015, 2007, 'STUDENT', 'CODE', '2026-03-08 08:00:00', 1);
INSERT INTO `course_member` VALUES (91043, 3015, 2008, 'STUDENT', 'CODE', '2026-03-08 08:05:00', 1);
INSERT INTO `course_member` VALUES (91044, 3015, 2009, 'STUDENT', 'CODE', '2026-03-08 08:10:00', 1);
INSERT INTO `course_member` VALUES (91045, 3015, 2013, 'STUDENT', 'CODE', '2026-03-08 08:15:00', 1);
INSERT INTO `course_member` VALUES (91051, 3016, 1002, 'TEACHER', 'CREATE', '2026-03-07 09:00:00', 1);
INSERT INTO `course_member` VALUES (91052, 3016, 2010, 'STUDENT', 'CODE', '2026-03-08 09:00:00', 1);
INSERT INTO `course_member` VALUES (91053, 3016, 2011, 'STUDENT', 'CODE', '2026-03-08 09:05:00', 1);
INSERT INTO `course_member` VALUES (91054, 3016, 2014, 'STUDENT', 'CODE', '2026-03-08 09:10:00', 1);
INSERT INTO `course_member` VALUES (91055, 3016, 2015, 'STUDENT', 'CODE', '2026-03-08 09:15:00', 1);
INSERT INTO `course_member` VALUES (91061, 3017, 1001, 'TEACHER', 'CREATE', '2026-03-08 14:00:00', 1);
INSERT INTO `course_member` VALUES (91062, 3017, 2001, 'STUDENT', 'CODE', '2026-03-09 10:00:00', 1);
INSERT INTO `course_member` VALUES (91063, 3017, 2003, 'STUDENT', 'CODE', '2026-03-09 10:05:00', 1);
INSERT INTO `course_member` VALUES (91064, 3017, 2005, 'STUDENT', 'CODE', '2026-03-09 10:10:00', 1);
INSERT INTO `course_member` VALUES (91065, 3017, 2014, 'STUDENT', 'CODE', '2026-03-09 10:15:00', 1);
INSERT INTO `course_member` VALUES (91071, 3018, 1002, 'TEACHER', 'CREATE', '2026-03-08 15:00:00', 1);
INSERT INTO `course_member` VALUES (91072, 3018, 2002, 'STUDENT', 'CODE', '2026-03-09 11:00:00', 1);
INSERT INTO `course_member` VALUES (91073, 3018, 2008, 'STUDENT', 'CODE', '2026-03-09 11:05:00', 1);
INSERT INTO `course_member` VALUES (91074, 3018, 2012, 'STUDENT', 'CODE', '2026-03-09 11:10:00', 1);
INSERT INTO `course_member` VALUES (91075, 3018, 2015, 'STUDENT', 'CODE', '2026-03-09 11:15:00', 1);

INSERT INTO `course_resource` VALUES (50201, 3011, '第1章 绪论课件', '数据库基本概念', 'DOC', '/files/res/db01.pdf', 'db_ch1.pdf', 524288, 'application/pdf', 1001, '2026-03-10 09:00:00', 1);
INSERT INTO `course_resource` VALUES (50202, 3011, 'SQL 练习数据集', '配套建表脚本', 'OTHER', '/files/res/db02.sql', 'practice.sql', 8192, 'text/plain', 1001, '2026-03-11 09:00:00', 1);
INSERT INTO `course_resource` VALUES (50203, 3011, '事务讲解视频', 'ACID 与隔离级别', 'VIDEO', '/files/res/db03.mp4', 'transaction.mp4', 10485760, 'video/mp4', 1001, '2026-03-12 09:00:00', 1);
INSERT INTO `course_resource` VALUES (50204, 3012, 'Flex 布局笔记', '小程序样式参考', 'DOC', '/files/res/web01.pdf', 'flex.pdf', 262144, 'application/pdf', 1001, '2026-03-10 10:00:00', 1);
INSERT INTO `course_resource` VALUES (50205, 3012, '小程序生命周期', '官方文档摘录', 'LINK', '/files/res/web02.md', 'lifecycle.md', 4096, 'text/markdown', 1001, '2026-03-11 10:00:00', 1);
INSERT INTO `course_resource` VALUES (50206, 3013, '集合框架思维导图', 'List/Set/Map', 'IMAGE', '/files/res/java01.png', 'collections.png', 153600, 'image/png', 1002, '2026-03-10 11:00:00', 1);
INSERT INTO `course_resource` VALUES (50207, 3013, 'IO 与 NIO 对比', '课件', 'DOC', '/files/res/java02.pdf', 'io_nio.pdf', 480000, 'application/pdf', 1002, '2026-03-11 11:00:00', 1);
INSERT INTO `course_resource` VALUES (50208, 3014, 'HTTP 协议要点', '请求响应与状态码', 'DOC', '/files/res/net01.pdf', 'http.pdf', 300000, 'application/pdf', 1002, '2026-03-12 11:00:00', 1);
INSERT INTO `course_resource` VALUES (50209, 3015, '进程与线程', '操作系统第三章', 'DOC', '/files/res/os01.pdf', 'process_thread.pdf', 400000, 'application/pdf', 1001, '2026-03-13 09:00:00', 1);
INSERT INTO `course_resource` VALUES (50210, 3016, '敏捷开发简介', 'Scrum 概要', 'DOC', '/files/res/se01.pdf', 'agile.pdf', 280000, 'application/pdf', 1002, '2026-03-13 10:00:00', 1);
INSERT INTO `course_resource` VALUES (50211, 3017, 'Python 环境安装', 'Anaconda 与 pip', 'DOC', '/files/res/py01.pdf', 'setup.pdf', 120000, 'application/pdf', 1001, '2026-03-14 09:00:00', 1);
INSERT INTO `course_resource` VALUES (50212, 3018, '二叉树遍历', '先序中序后序', 'DOC', '/files/res/ds01.pdf', 'tree.pdf', 350000, 'application/pdf', 1002, '2026-03-14 10:00:00', 1);

INSERT INTO `assignment` VALUES (62201, 3011, '作业1：单表查询', '完成指定表的增删改查', '2026-05-20 23:59:59', 100.00, 1001, NULL, '2026-03-15 09:00:00', 1);
INSERT INTO `assignment` VALUES (62202, 3011, '作业2：连接查询', '多表连接与分组统计', '2026-06-10 23:59:59', 100.00, 1001, NULL, '2026-03-20 09:00:00', 1);
INSERT INTO `assignment` VALUES (62203, 3012, '页面布局练习', '实现课程列表页', '2026-05-25 23:59:59', 100.00, 1001, NULL, '2026-03-16 09:00:00', 1);
INSERT INTO `assignment` VALUES (62204, 3012, '事件与数据绑定', '表单与列表联动', '2026-06-05 23:59:59', 100.00, 1001, NULL, '2026-03-18 09:00:00', 1);
INSERT INTO `assignment` VALUES (62205, 3013, '类与对象', '定义 Student 类', '2026-05-30 23:59:59', 100.00, 1002, NULL, '2026-03-17 09:00:00', 1);
INSERT INTO `assignment` VALUES (62206, 3013, '异常与泛型', '课堂练习', '2026-06-15 23:59:59', 100.00, 1002, NULL, '2026-03-22 09:00:00', 1);
INSERT INTO `assignment` VALUES (62207, 3014, '抓包分析', '使用 Wireshark', '2026-05-28 23:59:59', 100.00, 1002, NULL, '2026-03-19 09:00:00', 1);
INSERT INTO `assignment` VALUES (62208, 3015, '进程调度实验', '报告提交', '2026-06-01 23:59:59', 100.00, 1001, NULL, '2026-03-21 09:00:00', 1);
INSERT INTO `assignment` VALUES (62209, 3016, '用例图作业', '在线选课场景', '2026-06-08 23:59:59', 100.00, 1002, NULL, '2026-03-23 09:00:00', 1);
INSERT INTO `assignment` VALUES (62210, 3017, '列表与字典', '数据处理小练习', '2026-05-22 23:59:59', 100.00, 1001, NULL, '2026-03-24 09:00:00', 1);
INSERT INTO `assignment` VALUES (62211, 3018, '排序算法实现', '快排与归并', '2026-06-20 23:59:59', 100.00, 1002, NULL, '2026-03-25 09:00:00', 1);

INSERT INTO `assignment_submission` VALUES (73201, 62201, 2001, '已完成单表查询练习', NULL, '2026-03-28 20:00:00', 'SUBMITTED', 0, 1, '2026-03-28 20:00:00');
INSERT INTO `assignment_submission` VALUES (73202, 62201, 2002, '见附件说明', NULL, '2026-03-29 15:30:00', 'SUBMITTED', 0, 1, '2026-03-29 15:30:00');
INSERT INTO `assignment_submission` VALUES (73203, 62201, 2003, 'SQL 脚本在正文', NULL, '2026-03-30 10:00:00', 'SUBMITTED', 0, 1, '2026-03-30 10:00:00');
INSERT INTO `assignment_submission` VALUES (73204, 62203, 2002, '小程序页面截图说明', NULL, '2026-04-01 18:00:00', 'SUBMITTED', 0, 1, '2026-04-01 18:00:00');
INSERT INTO `assignment_submission` VALUES (73205, 62205, 2004, 'Student.java 源码', NULL, '2026-04-02 12:00:00', 'SUBMITTED', 0, 1, '2026-04-02 12:00:00');
INSERT INTO `assignment_submission` VALUES (73206, 62205, 2005, '略迟提交', NULL, '2026-04-03 22:00:00', 'SUBMITTED', 1, 1, '2026-04-03 22:00:00');

INSERT INTO `assignment_grade` VALUES (74201, 73201, 1001, 92.00, '书写规范，结果正确', '2026-04-01 09:00:00');
INSERT INTO `assignment_grade` VALUES (74202, 73202, 1001, 85.50, '注意日期函数用法', '2026-04-01 09:30:00');
INSERT INTO `assignment_grade` VALUES (74203, 73203, 1001, 78.00, '部分查询可优化', '2026-04-01 10:00:00');
INSERT INTO `assignment_grade` VALUES (74204, 73204, 1001, 88.00, '布局清晰', '2026-04-02 14:00:00');
INSERT INTO `assignment_grade` VALUES (74205, 73205, 1002, 95.00, '代码完整', '2026-04-03 10:00:00');
INSERT INTO `assignment_grade` VALUES (74206, 73206, 1002, 72.00, '迟交扣分', '2026-04-04 11:00:00');

INSERT INTO `checkin` VALUES (15301, 3011, '第3周课堂签到', '31001', '2026-03-18 08:00:00', '2026-03-18 09:40:00', 1001, 1, '2026-03-18 07:55:00');
INSERT INTO `checkin` VALUES (15302, 3011, '第5周课堂签到', '31002', '2026-04-01 08:00:00', '2026-04-01 09:40:00', 1001, 1, '2026-04-01 07:55:00');
INSERT INTO `checkin` VALUES (15303, 3012, '第2周签到', '32001', '2026-03-20 14:00:00', '2026-03-20 15:40:00', 1001, 1, '2026-03-20 13:55:00');
INSERT INTO `checkin` VALUES (15304, 3013, 'Java 周测签到', '33001', '2026-03-25 10:00:00', '2026-03-25 11:40:00', 1002, 1, '2026-03-25 09:55:00');
INSERT INTO `checkin` VALUES (15305, 3014, '网络课签到', '34001', '2026-03-26 08:30:00', '2026-03-26 10:00:00', 1002, 1, '2026-03-26 08:25:00');
INSERT INTO `checkin` VALUES (15306, 3015, '操作系统实验签到', '35001', '2026-03-27 13:00:00', '2026-03-27 16:00:00', 1001, 1, '2026-03-27 12:55:00');
INSERT INTO `checkin` VALUES (15307, 3018, '算法课签到', '38001', '2026-03-28 09:00:00', '2026-03-28 10:40:00', 1002, 1, '2026-03-28 08:55:00');

INSERT INTO `checkin_record` VALUES (15401, 15301, 2001, '2026-03-18 08:15:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15402, 15301, 2002, '2026-03-18 08:18:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15403, 15301, 2003, '2026-03-18 08:20:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15404, 15302, 2001, '2026-04-01 08:10:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15405, 15302, 2004, '2026-04-01 08:12:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15406, 15303, 2002, '2026-03-20 14:05:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15407, 15304, 2004, '2026-03-25 10:05:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15408, 15304, 2005, '2026-03-25 10:08:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15409, 15307, 2002, '2026-03-28 09:05:00', 'CLICK');
INSERT INTO `checkin_record` VALUES (15410, 15307, 2012, '2026-03-28 09:06:00', 'CLICK');

INSERT INTO `learning_progress` VALUES (16301, 50201, 2001, 2, 100, '2026-03-20 10:00:00', '2026-03-20 10:00:00');
INSERT INTO `learning_progress` VALUES (16302, 50202, 2001, 2, 80, '2026-03-21 11:00:00', '2026-03-21 11:00:00');
INSERT INTO `learning_progress` VALUES (16303, 50201, 2002, 2, 100, '2026-03-20 15:00:00', '2026-03-20 15:00:00');
INSERT INTO `learning_progress` VALUES (16304, 50203, 2003, 1, 45, '2026-03-22 09:00:00', '2026-03-22 09:00:00');
INSERT INTO `learning_progress` VALUES (16305, 50204, 2002, 2, 100, '2026-03-23 14:00:00', '2026-03-23 14:00:00');
INSERT INTO `learning_progress` VALUES (16306, 50206, 2004, 2, 100, '2026-03-24 10:00:00', '2026-03-24 10:00:00');
INSERT INTO `learning_progress` VALUES (16307, 50208, 2001, 0, 20, '2026-03-25 16:00:00', '2026-03-25 16:00:00');
INSERT INTO `learning_progress` VALUES (16308, 50212, 2002, 2, 90, '2026-03-26 20:00:00', '2026-03-26 20:00:00');

INSERT INTO `course_final_grade` VALUES (96001, 3011, 2001, 92.00, 100.00, 95.00, 82.00, 89.50, '2026-04-05 18:00:00');
INSERT INTO `course_final_grade` VALUES (96002, 3011, 2002, 85.50, 100.00, 88.00, 76.00, 84.20, '2026-04-05 18:00:00');
INSERT INTO `course_final_grade` VALUES (96003, 3011, 2003, 78.00, 100.00, 70.00, 65.00, 75.10, '2026-04-05 18:00:00');
INSERT INTO `course_final_grade` VALUES (96004, 3012, 2002, 88.00, 100.00, 100.00, 0.00, 89.80, '2026-04-05 18:00:00');
INSERT INTO `course_final_grade` VALUES (96005, 3013, 2004, 95.00, 100.00, 92.00, 88.00, 92.40, '2026-04-05 18:00:00');
INSERT INTO `course_final_grade` VALUES (96006, 3013, 2005, 72.00, 100.00, 85.00, 70.00, 76.50, '2026-04-05 18:00:00');
INSERT INTO `course_final_grade` VALUES (96007, 3014, 2001, 90.00, 100.00, 100.00, 0.00, 91.00, '2026-04-05 18:00:00');
INSERT INTO `course_final_grade` VALUES (96008, 3018, 2002, 86.00, 100.00, 90.00, 79.00, 85.60, '2026-04-05 18:00:00');

INSERT INTO `exam_paper` VALUES (88301, 3011, '数据库期中测验', 60, '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1001, 1, '2026-03-28 10:00:00');
INSERT INTO `exam_paper` VALUES (88302, 3011, 'SQL 基础小测', 30, '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1001, 1, '2026-03-29 10:00:00');
INSERT INTO `exam_paper` VALUES (88303, 3011, '第7章草稿卷', 45, NULL, NULL, 1001, 0, '2026-03-30 10:00:00');
INSERT INTO `exam_paper` VALUES (88304, 3013, 'Java 基础测验', 45, '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1002, 1, '2026-03-29 14:00:00');
INSERT INTO `exam_paper` VALUES (88305, 3018, '数据结构单元测', 90, '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1002, 1, '2026-03-30 09:00:00');

INSERT INTO `exam_question` VALUES (89301, 88301, 1, '关系数据库的三大范式主要解决什么问题？', 5.00, 1, 'B', 1);
INSERT INTO `exam_question` VALUES (89302, 88301, 1, '下列哪一项是事务的 ACID 特性之一？', 5.00, 2, 'A', 1);
INSERT INTO `exam_question` VALUES (89303, 88301, 3, 'DELETE 语句一定会触发触发器（判断）', 5.00, 3, 'F', 1);
INSERT INTO `exam_question` VALUES (89304, 88302, 1, 'SELECT 语句中用于去重的关键字是？', 5.00, 1, 'C', 1);
INSERT INTO `exam_question` VALUES (89305, 88302, 3, '主键可以包含 NULL（判断）', 5.00, 2, 'F', 1);
INSERT INTO `exam_question` VALUES (89306, 88304, 1, 'Java 中 int 的包装类是？', 5.00, 1, 'A', 1);
INSERT INTO `exam_question` VALUES (89307, 88304, 3, '接口可以包含具体实现的方法（Java8+）（判断）', 5.00, 2, 'T', 1);
INSERT INTO `exam_question` VALUES (89308, 88305, 1, '二分查找的前提条件是？', 5.00, 1, 'A', 1);
INSERT INTO `exam_question` VALUES (89309, 88305, 3, '栈是先进先出结构（判断）', 5.00, 2, 'F', 1);

INSERT INTO `exam_option` VALUES (90301, 89301, 'A', '提高查询速度', 1, 1);
INSERT INTO `exam_option` VALUES (90302, 89301, 'B', '减少数据冗余与更新异常', 2, 1);
INSERT INTO `exam_option` VALUES (90303, 89301, 'C', '增强安全性', 3, 1);
INSERT INTO `exam_option` VALUES (90304, 89301, 'D', '简化 SQL 语法', 4, 1);
INSERT INTO `exam_option` VALUES (90305, 89302, 'A', '原子性', 1, 1);
INSERT INTO `exam_option` VALUES (90306, 89302, 'B', '可逆性', 2, 1);
INSERT INTO `exam_option` VALUES (90307, 89302, 'C', '并行性', 3, 1);
INSERT INTO `exam_option` VALUES (90308, 89302, 'D', '可扩展性', 4, 1);
INSERT INTO `exam_option` VALUES (90309, 89304, 'A', 'UNIQUE', 1, 1);
INSERT INTO `exam_option` VALUES (90310, 89304, 'B', 'GROUP', 2, 1);
INSERT INTO `exam_option` VALUES (90311, 89304, 'C', 'DISTINCT', 3, 1);
INSERT INTO `exam_option` VALUES (90312, 89304, 'D', 'ORDER', 4, 1);
INSERT INTO `exam_option` VALUES (90313, 89306, 'A', 'Integer', 1, 1);
INSERT INTO `exam_option` VALUES (90314, 89306, 'B', 'int（小写）不可作为类名', 2, 1);
INSERT INTO `exam_option` VALUES (90315, 89306, 'C', 'Int', 3, 1);
INSERT INTO `exam_option` VALUES (90316, 89306, 'D', 'Number', 4, 1);
INSERT INTO `exam_option` VALUES (90317, 89308, 'A', '有序数组', 1, 1);
INSERT INTO `exam_option` VALUES (90318, 89308, 'B', '链表长度固定', 2, 1);
INSERT INTO `exam_option` VALUES (90319, 89308, 'C', '图连通', 3, 1);
INSERT INTO `exam_option` VALUES (90320, 89308, 'D', '无要求', 4, 1);

INSERT INTO `exam_attempt` VALUES (91301, 88301, 2001, '2026-04-02 10:00:00', '2026-04-02 10:25:00', 15.00, 15.00, 2);
INSERT INTO `exam_attempt` VALUES (91302, 88301, 2002, '2026-04-03 14:00:00', '2026-04-03 14:20:00', 10.00, 10.00, 2);
INSERT INTO `exam_attempt` VALUES (91303, 88304, 2004, '2026-04-04 09:00:00', '2026-04-04 09:15:00', 10.00, 10.00, 2);

INSERT INTO `exam_answer` VALUES (92301, 91301, 89301, 'B', 5.00);
INSERT INTO `exam_answer` VALUES (92302, 91301, 89302, 'A', 5.00);
INSERT INTO `exam_answer` VALUES (92303, 91301, 89303, 'F', 5.00);
INSERT INTO `exam_answer` VALUES (92304, 91302, 89301, 'A', 0.00);
INSERT INTO `exam_answer` VALUES (92305, 91302, 89302, 'A', 5.00);
INSERT INTO `exam_answer` VALUES (92306, 91302, 89303, 'T', 0.00);
INSERT INTO `exam_answer` VALUES (92307, 91303, 89306, 'A', 5.00);
INSERT INTO `exam_answer` VALUES (92308, 91303, 89307, 'T', 5.00);

INSERT INTO `recommendation_push` VALUES (93301, 1001, 3011, 2003, 50203, '你资源完成率偏低，推荐补看事务视频', 0, NULL, '2026-03-25 12:00:00');
INSERT INTO `recommendation_push` VALUES (93302, 1001, 3012, 2007, 50205, '建议复习生命周期章节', 1, '2026-03-26 08:00:00', '2026-03-26 07:00:00');
INSERT INTO `recommendation_push` VALUES (93303, 1002, 3013, 2009, 50207, 'IO 章节与作业相关', 0, NULL, '2026-03-27 15:00:00');
INSERT INTO `recommendation_push` VALUES (93304, 1002, 3018, 2015, 50212, '排序作业前请完成二叉树课件', 0, NULL, '2026-03-28 16:00:00');

INSERT INTO `submission_file` VALUES (74301, 73202, '/files/submissions/73202/hw1.zip', 'homework1.zip', 120000, 'application/zip', '2026-03-29 15:30:00');

SET FOREIGN_KEY_CHECKS = 1;
