package controllers.auth

import common.actions.LoggedRequest
import common.actions.RequestType._
import models.user.UserRepo
import models.auth._
import services.auth.SessionService
import controllers.AbstractController
import common.FutureEither._

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.i18n.Langs


class LogoutController(sessionService: SessionService,
                       loginCredentialRepo: LoginCredentialRepo,
                       userRepo: UserRepo,
                       langs: Langs, 
                       cc: ControllerComponents) extends AbstractController(sessionService, userRepo, cc) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def logout(userId: Long): Action[AnyContent] = LoggedAction(userId, Api).async { implicit request => 
    Logger.debug("Logout attempt of user #" + request.user.id)

    sessionService.deleteSession(authToken = request.authToken).map { deleted =>
      if (deleted) loginCredentialRepo.logout(authToken = request.authToken).fold(badRequest(_), _ => ok(JsNull))
      else badRequest("couldn't remove the session")
    }
  }
}
