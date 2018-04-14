package controllers.user

import services.auth.SessionService
import controllers.AbstractController

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.i18n.Langs

import models.user.UserProfileRepo._
import models.user.requests.UserProfileRequest
import models.user.{UserProfileRepo, UserRepo}
import common.actions.RequestType._

import scala.util.{Left, Right}

class UserProfileController(sessionService: SessionService,
                            userRepo: UserRepo,
                            userProfileRepo: UserProfileRepo,
                            langs: Langs, 
                            cc: ControllerComponents) extends AbstractController(sessionService, userRepo, cc) {

  def updateUserProfile(userId: Long): Action[JsValue] = LoggedAction(userId, Api)(parse.json) { implicit request =>
    Logger.debug("User update attempt of: " + Json.prettyPrint(request.body))
    request.body.validate[UserProfileRequest] match {
      case s: JsSuccess[UserProfileRequest] =>
        val userProfileRequest: UserProfileRequest = s.get
        userProfileRepo.updateProfile(userId, userProfileRequest).fold(
          error => badRequest(error),
          userProfile => ok("", Json.toJson(userProfile))
        )
      case e: JsError =>
        Logger.error(s"UserProfileController updateUserProfile: $e")
        badRequest("error.invalid.userProfileRequest")
    }
  }

}
