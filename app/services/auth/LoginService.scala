package services.auth

import common.auth.AuthValidator._
import security.PasswordManager
import play.api.Configuration
import play.api.mvc.Request
import com.github.nscala_time.time.Imports._
import play.api._
import models.user.{User, UserRepo}
import models.auth.{LoginCredential, LoginCredentialRepo}
import models.auth.LoginCredentialStatus

class LoginService(config: Configuration,
                   userRepo: UserRepo,
                   loginCredentialRepo: LoginCredentialRepo) {

  lazy val authTokenExpirationDuration = config.get[Int]("app.authToken.expiration.duration")

  def login(email: String, password: String)(implicit request: Request[_]): Either[String, LoginCredential] = {
    Logger.trace(s"LoginService email: $email")
    userRepo.findByEmail(email) match {
      case Some(user) => doLogin(user, password)
      case None => Left("email.notfound")
    }
  }

  // Private methods

  private def generateAuthToken = java.util.UUID.randomUUID.toString

  private def doLogin(user: User,
                      password: String)(implicit request: Request[_]): Either[String, LoginCredential] = {
    for {
      _ <- PasswordManager.checkPassword(password, user.password).right
      loginCredential <- executeLogin(user.id).right
    } yield loginCredential
  }

  private def executeLogin(userId: Long)(implicit request: Request[_]): Either[String, LoginCredential] = {
    val authToken = generateAuthToken
    val expirationDate = DateTime.now(DateTimeZone.UTC) + authTokenExpirationDuration.days
    for {
      loginCredential <- loginCredentialRepo.save(userId, authToken, LoginCredentialStatus.ENABLED, expirationDate)
        .toRight(s"couldnt save the login credential for user $userId").right
    } yield loginCredential
  }

}
