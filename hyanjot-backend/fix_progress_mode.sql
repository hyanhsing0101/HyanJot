-- 修复 todo_item 表，使 progress_mode 列允许 NULL
-- 在 MySQL 中执行此 SQL 语句

ALTER TABLE todo_item MODIFY COLUMN progress_mode BOOLEAN NULL;

