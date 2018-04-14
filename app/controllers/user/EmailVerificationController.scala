package controllers.user

import services.auth.SessionService
import play.api.libs.json._
import play.api.Logger
import play.api.mvc._

import com.github.nscala_time.time.Imports._

import models.user.{EmailVerificationRepo, UserRepo, EmailVerification}
import controllers.AbstractController
import services.user.UserEmailService

import common.actions.RequestType._
import controllers.AbstractController
import play.api.i18n.Langs


class EmailVerificationController(userEmailService: UserEmailService,
                                  sessionService: SessionService,
                                  userRepo: UserRepo,
                                  emailVerificationRepo: EmailVerificationRepo,
                                  langs: Langs, 
                                  cc: ControllerComponents) extends AbstractController(sessionService, userRepo, cc) {

  def verifyEmail(hash: String): Action[AnyContent] = MaybeLoggedAction(Api) { request =>
    resolveEmailVerification(hash).fold(error => badRequest(error), message => ok(message, JsNull))
  }

  def resendEmailVerification(userId: Long): Action[AnyContent] = LoggedAction(userId, Api) { implicit request =>
    Logger.debug(s"EmailVerificationController resendEmailVerification user #$userId")
    userEmailService.sendVerificationEmail(userId).fold(
      error => badRequest(error),
      reponse => ok("email.verification.sent", JsNull)
    )
  }

  private def resolveEmailVerification(hash: String): Either[String, String] = {
    for {
      emailVerification <- isValidHash(hash).right
      emailVerificationUpdate <- emailVerificationRepo.updateVerificationStatusVerified(emailVerification.id).right
      userVerificationStatus <- userRepo.updateVerificationStatusActive(emailVerification.userId).right
    } yield "emailVerification.verified"
  }

  private def isValidHash(hash: String): Either[String, EmailVerification] = {
    emailVerificationRepo.findByHash(hash) match {
      case Some(emailVerification: EmailVerification) =>
        if (emailVerification.expirationDate.isBefore(DateTime.now)) Left("emailVerification.error.hashExpired")
        else if (emailVerification.verificationStatus == "verified") Left("emailVerification.error.alreadyVerified")
        else Right(emailVerification)
      case _ => Left("emailVerification.error.hashNotFound")
    }
  }

}
