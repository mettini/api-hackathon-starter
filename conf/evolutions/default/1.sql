# noinspection SqlNoDataSourceInspectionForFile
# DROP DATABASE IF EXISTS hackathonStarter;
# CREATE DATABASE hackathonStarter DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
# USE hackathonStarter;

# --- !Ups
CREATE TABLE IF NOT EXISTS users(
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  verification_status VARCHAR(150) NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  creation_date DATETIME NOT NULL,
  last_modification_date DATETIME NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_configs(
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id INT(11) UNSIGNED NOT NULL,
  is_test BOOLEAN NOT NULL DEFAULT FALSE,
  is_moderated BOOLEAN NOT NULL DEFAULT FALSE,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  creation_date DATETIME NOT NULL,
  last_modification_date DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS email_verifications(
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id INT(11) UNSIGNED NOT NULL,
  email VARCHAR(254) NOT NULL,
  verification_code VARCHAR(60) NOT NULL,
  verification_status VARCHAR(50) NOT NULL,
  is_deleted BOOLEAN NOT NULL,
  expiration_date DATETIME NOT NULL,
  creation_date DATETIME NOT NULL,
  last_modification_date DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY IX_email_verifications_hash (verification_code)
);

CREATE TABLE IF NOT EXISTS email_password_resets(
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id INT(11) UNSIGNED NOT NULL,
  verification_code VARCHAR(60) NOT NULL,
  used BOOLEAN NOT NULL,
  is_deleted BOOLEAN NOT NULL,
  expiration_date DATETIME NOT NULL,
  reseted_date DATETIME DEFAULT NULL,
  creation_date DATETIME NOT NULL,
  last_modification_date DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY IX_email_password_resets_hash (verification_code)
);

CREATE TABLE IF NOT EXISTS user_profiles(
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id INT(11) UNSIGNED NOT NULL UNIQUE,
  firstname VARCHAR(255) NOT NULL,
  lastname VARCHAR(255) DEFAULT NULL,
  is_deleted BOOLEAN NOT NULL,
  creation_date DATETIME NOT NULL,
  last_modification_date DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS login_credentials(
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id INT(11) UNSIGNED NOT NULL,
  auth_token VARCHAR(150) NOT NULL,
  status VARCHAR(50) NOT NULL,
  expiration_date DATETIME NOT NULL,
  is_deleted BOOLEAN NOT NULL,
  creation_date DATETIME NOT NULL,
  last_modification_date DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY IX_login_credentials_auth_token (auth_token),
  KEY IX_login_credentials_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS sendgrid_email_templates(
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  template_id VARCHAR(255) NOT NULL,
  subject VARCHAR(255) NOT NULL,
  body_html TEXT NOT NULL,
  body_text TEXT NOT NULL,
  is_deleted BOOLEAN NOT NULL,
  creation_date DATETIME NOT NULL,
  last_modification_date DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE(name),
  UNIQUE(template_id)
);

ALTER TABLE email_verifications
  ADD CONSTRAINT FK_email_verifications_users
      FOREIGN KEY (user_id)
      REFERENCES users (id);

ALTER TABLE user_profiles
  ADD CONSTRAINT FK_user_profiles_users
    FOREIGN KEY (user_id)
    REFERENCES users (id);

ALTER TABLE login_credentials
  ADD CONSTRAINT FK_login_credentials_users
    FOREIGN KEY (user_id)
    REFERENCES users (id);

ALTER TABLE user_configs
  ADD CONSTRAINT FK_user_configs_user
    FOREIGN KEY (user_id)
    REFERENCES users (id);

# --- !Downs
DROP TABLE users;
DROP TABLE user_configs;
DROP TABLE email_verifications;
DROP TABLE email_password_resets;
DROP TABLE user_profiles;
DROP TABLE login_credentials;
DROP TABLE sendgrid_email_templates;
