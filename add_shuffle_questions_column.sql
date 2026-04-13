-- 为 exam_paper 表补充 shuffle_questions 字段（兼容 MySQL 5.7+）
-- 执行后可支持“打乱题目顺序”配置持久化。

SET @db_name = DATABASE();

SET @col_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'exam_paper'
    AND COLUMN_NAME = 'shuffle_questions'
);

SET @ddl = IF(
  @col_exists = 0,
  'ALTER TABLE exam_paper ADD COLUMN shuffle_questions TINYINT NOT NULL DEFAULT 0 COMMENT ''是否打乱题目顺序：0否 1是''',
  'SELECT ''shuffle_questions already exists'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

