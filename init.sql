CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS userdb;
CREATE DATABASE IF NOT EXISTS resume_db;
CREATE DATABASE IF NOT EXISTS paymentdb;
CREATE DATABASE IF NOT EXISTS notificationdb;
CREATE DATABASE IF NOT EXISTS ai_db;

-- auth_service
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_pass';
GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';

-- user_service
CREATE USER IF NOT EXISTS 'user_user'@'%' IDENTIFIED BY 'user_pass';
GRANT ALL PRIVILEGES ON userdb.* TO 'user_user'@'%';

-- resume_service
CREATE USER IF NOT EXISTS 'resume_user'@'%' IDENTIFIED BY 'resume_pass';
GRANT ALL PRIVILEGES ON resume_db.* TO 'resume_user'@'%';

-- payment_service
CREATE USER IF NOT EXISTS 'payment_user'@'%' IDENTIFIED BY 'payment_pass';
GRANT ALL PRIVILEGES ON paymentdb.* TO 'payment_user'@'%';

-- notification_service
CREATE USER IF NOT EXISTS 'notification_user'@'%' IDENTIFIED BY 'notification_pass';
GRANT ALL PRIVILEGES ON notificationdb.* TO 'notification_user'@'%';

-- ai_service
CREATE USER IF NOT EXISTS 'ai_user'@'%' IDENTIFIED BY 'ai_pass';
GRANT ALL PRIVILEGES ON ai_db.* TO 'ai_user'@'%';

FLUSH PRIVILEGES;
