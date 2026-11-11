-- Runs once, on the first start of the container
CREATE DATABASE IF NOT EXISTS alert_db;  -- alert-service (Member 5)
CREATE DATABASE IF NOT EXISTS user_db;   -- user-service (Member 6)
GRANT ALL PRIVILEGES ON alert_db.* TO 'meteo'@'%';
GRANT ALL PRIVILEGES ON user_db.* TO 'meteo'@'%';
FLUSH PRIVILEGES;
