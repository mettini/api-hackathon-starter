package controllers.user

import services.auth.SessionService
import controllers.AbstractController
import models.user._
import security.PasswordManager
import services.user.UserEmailService
import common.auth.AuthValidator._
import common.actions.RequestType._
import common.FutureEither
import common.FutureEither._

import play.api.libs.json._
import play.api.mvc._
import play.api.Configuration
import com.github.nscala_time.time.Imports._
import play.api.i18n.Langs


class ChangePasswordController(userEmailService: UserEmailService,
                               config: Configuration,
                               sessionService: SessionService,
                               userRepo: UserRepo,
                               emailResetPasswordRepo: EmailResetPasswordRepo,
                               langs: Langs, 
                               cc: ControllerComponents) extends AbstractController(sessionService, userRepo, cc) {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val emailResetPasswordSeed = config.get[String]("app.reset-pasword.seed")

  def changePasswordRequest: Action[JsValue] = MaybeLoggedAction(Api).async(parse.json) { implicit request =>
    resolveChangePassword(request.body).fold(badRequest(_), changePasswordRequestOk)
  }

  def changePassword: Action[JsValue] = MaybeLoggedAction(Api)(parse.json) { implicit request =>
    (for {
      verificationCode <- obtainJsonField[String]("verificationCode", request.body).right
      newPass <- obtainJsonField[String]("password", request.body).right
      _ <- validatePassword(newPass).right
      userId <- changePasswordByVerificationCode(verificationCode, newPass).right
    } yield userId).fold(error => badRequest(error), userId => ok(Json.obj("userId" -> userId)))
  }

  def changePasswordData(verificationCode: String): Action[AnyContent] = MaybeLoggedAction(Api) { implicit request =>
    emailResetPasswordRepo.findByVerificationCode(verificationCode).map { emailResetPassword =>
      ok("", Json.obj(
        "verificationCode" -> emailResetPassword.hash,
        "used" -> emailResetPassword.used,
        "expired" -> (emailResetPassword.expirationDate < DateTime.now)))
    }.getOrElse {
      badRequest("invalid.verification.code")
    }
  }

  def changePassword(erp: EmailResetPassword, newPass: String): Either[String, Long] = {
    (for {
      emailResetPass <- validEmailResetPassword(Some(erp)).right
      _ <- userRepo.updatePassword(emailResetPass.userId, PasswordManager.encodePassword(newPass)).right
      _ <- emailResetPasswordRepo.passwordReseted(emailResetPass.id).right
    } yield emailResetPass.userId).fold(error => Left(error), userId => Right(userId))
  }

  private def changePasswordRequestOk(verificationCode: String) = ok("", Json.obj("verificationCode" -> verificationCode))

  private def resolveChangePassword(body: JsValue): FutureEither[String, String] = {
    obtainJsonField[String]("email", body) match {
      case Right(email: String) if isValidEmail(email) => sendResetPasswordMail(email).toFutureEither
      case _ => Left[String, String]("invalid.email").toFutureEither
    }
  }

  private def sendResetPasswordMail(email: String): Either[String, String] = {
    userRepo.findByEmail(email) match {
      case Some(user) => userEmailService.sendResetPasswordMail(user.id)
      case _ => Left("email.notfound")
    }
  }

  private def validEmailResetPassword(erp: Option[EmailResetPassword]): Either[String, EmailResetPassword] = {
    erp match {
      case Some(resetPass) if resetPass.used => Left("resetpasswordhash.already.used")
      case Some(resetPass) if resetPass.expirationDate < DateTime.now => Left("resetpasswordhash.expired")
      case Some(resetPass) => Right(resetPass)
      case None => Left("invalid.resetpasswordhash")
    }
  }

  private def changePasswordByVerificationCode(verificationCode: String, newPass: String) = {
    emailResetPasswordRepo.findByVerificationCode(verificationCode) map { ecp => 
      changePassword(ecp, newPass)
    } getOrElse {
      Left("invalid.verification.code")
    }
  }

}
