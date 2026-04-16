#!/bin/bash
# 按正确顺序初始化数据库表结构和词汇数据
# 此脚本由 MySQL Docker 容器在首次启动时自动执行
set -e

DIR="/sql-scripts"

echo ">>> [1/5] 初始化表结构..."
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/create_new.sql"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/creat_wordList.sql"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/daily_new.sql"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/private_new.sql"

echo ">>> [2/5] 导入词书元数据..."
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/WordBooks.sql"

echo ">>> [3/5] 导入词汇数据（文件较大，约需 1-3 分钟）..."
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/CET6_patched_v2.sql"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/TOEFL_patched_v2.sql"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/GRE_patched_v2.sql"

echo ">>> [4/5] 应用数据补丁..."
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/compete_init.sql"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/update_missing_tables.sql"

echo ">>> [5/5] 导入测试数据..."
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$DIR/test_data.sql"

echo ">>> 数据库初始化完成！测试账号: test_user / 123456"
