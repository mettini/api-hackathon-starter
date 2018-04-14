package controllers.auth

import common.auth.AuthValidator._
import common.actions.RequestType._
import common.FutureEither._
import models.auth.{LoginCredential, LoginCredentialRepo}
import models.user.UserRepo
import services.auth._
import controllers.AbstractController

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.i18n.Langs


class LoginController(userRepo: UserRepo,
                      sessionService: SessionService,
                      loginService: LoginService,
                      langs: Langs, 
                      cc: ControllerComponents) extends AbstractController(sessionService, userRepo, cc) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def login: Action[JsValue] = MaybeLoggedAction(Api).async(parse.json) { implicit request =>
    Logger.debug(s"LoginController login")

    (for {
      email <- obtainJsonField[String]("email", request.body).toFutureEither
      password <- obtainJsonField[String]("password", request.body).toFutureEither
      _ <- validatePassword(password).toFutureEither
      loginCredential <- loginService.login(email, password).toFutureEither
    } yield loginCredential).fold(
      badRequest(_),
      loginCredential => Ok(Json.obj("error" -> false, "authToken" -> loginCredential.authToken, "userId" -> loginCredential.userId))
    )
  }
}
