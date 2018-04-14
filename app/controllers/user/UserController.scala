package controllers.user

import anorm.NamedParameter
import common.auth.AuthValidator
import models.auth.LoginCredentialRepo
import services.auth.SessionService

import controllers.AbstractController
import common.formatters.UserFormatter.userWrites
import common.FutureEither._
import common.actions.RequestType._

import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.i18n.Langs

import services.user.UserEmailService
import models.user.User.UserVerificationStatus
import models.user.UserProfileRepo._
import models.user._

import scala.collection.breakOut

case class UserController(sessionService: SessionService,
                          userRepo: UserRepo,
                          userEmailService: UserEmailService,
                          userProfileRepo: UserProfileRepo,
                          langs: Langs, 
                          cc: ControllerComponents,
                          loginCredentialRepo: LoginCredentialRepo) extends AbstractController(sessionService, userRepo, cc) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def delete(userId: Long): Action[AnyContent] = LoggedAction(userId, Api) { request =>
    Logger.debug(s"UserController delete userId #$userId")
    (for {
      user <- userRepo.habeasData(userId).right
      loginCredential <- loginCredentialRepo.habeasData(userId).right
    } yield Json.obj("userId" -> userId, "deleted" -> true)).fold(
      error => badRequest(error),
      response => ok("", response)
    )
  }

  def updateEmail(userId: Long): Action[JsValue] = { LoggedAction(userId, Api)(parse.json) { request =>
    (for {
      email <- obtainJsonField[String]("email", request.body).right
      _ <- AuthValidator.validateEmail(email).right
      _ <- userRepo.findByEmail(email).map(_ => "email.already.used").toLeft("").right
      update <- userRepo.update(userId, Seq(NamedParameter("email", email),
        NamedParameter("verification_status", UserVerificationStatus.Pending.toString))).right
      _ <- userEmailService.sendVerificationEmail(userId).right
    } yield update).fold (
      error =>  badRequest(error),
      update => if (update == 1) ok("", JsNull) else badRequest("not.updated")
    )
  }}

  def get(userId: Long): Action[AnyContent] = LoggedAction(userId, Api).async { implicit request =>
    Logger.debug(s"User get #$userId ")
    (for {
      user <- userRepo.findById(userId).toRight("user.error.userIdNotFound").toFutureEither
      userProfile <- userProfileRepo.findByUserId(userId).toRight("user.error.userProfileNotFound").toFutureEither
    } yield buildUserResponse(user, userProfile)).fold(badRequest(_), response => ok("", response))
  }

  private def buildUserResponse(user: User, userProfile: UserProfile): JsValue = {
    Json.obj("user" -> user, "userProfile" -> Json.toJson(userProfile).as[JsObject])
  }

}
