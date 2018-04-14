package models

import play.api.db.{DBComponents, HikariCPComponents}

trait ModelsModule
  extends HikariCPComponents
  with DBComponents {

  import com.softwaremill.macwire._
  import models.user.{UserConfigRepo, UserRepo, UserProfileRepo, EmailVerificationRepo, EmailResetPasswordRepo}
  import models.auth.{LoginCredentialRepo}
  import models.email.{EmailTemplateRepo}

  val db = dbApi.database("default")

  lazy val userConfigRepo = wire[UserConfigRepo]
  lazy val userRepo = wire[UserRepo]
  lazy val userProfileRepo = wire[UserProfileRepo]
  lazy val emailVerificationRepo = wire[EmailVerificationRepo]
  lazy val emailResetPasswordRepo = wire[EmailResetPasswordRepo]
  lazy val loginCredentialRepo = wire[LoginCredentialRepo]
  lazy val emailTemplateRepo = wire[EmailTemplateRepo]

}
