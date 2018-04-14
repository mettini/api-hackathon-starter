package controllers.auth

import models.auth.LoginCredentialRepo
import models.auth.requests.SignupRequest
import models.user.UserRepo
import services.auth._
import common.actions.RequestType._
import common.FutureEither._
import controllers.AbstractController

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.i18n.Langs


class SignupController(signupService: SignupService,
                       sessionService: SessionService,
                       userRepo: UserRepo,
                       loginCredentialRepo: LoginCredentialRepo,
                       langs: Langs, 
                       cc: ControllerComponents) extends AbstractController(sessionService, userRepo, cc) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def signup: Action[JsValue] = MaybeLoggedAction(Api).async(parse.json) { implicit request =>
    Logger.debug(s"SignupController attempt")

    (for {
      signupRequest <- resolveRequest(request.body).toFutureEither
      loginCredential <- signupService.signup(signupRequest).toFutureEither
    } yield loginCredential).fold(
      error => badRequest(error),
      loginCredential => Ok(Json.obj("error" -> false, "authToken" -> loginCredential.authToken, "userId" -> loginCredential.userId))
    )
  }

  private def resolveRequest(body: JsValue): Either[String, SignupRequest] = {
    for {
      firstname <- obtainJsonField[String]("firstname", body).right
      lastname <- obtainJsonField[String]("lastname", body).right
      email <- obtainJsonField[String]("email", body).right
      password <- obtainJsonField[String]("password", body).right
      signupRequest <- Right(SignupRequest(email, password, firstname, lastname)).right
      _ <- signupRequest.validateInput.right
    } yield signupRequest
  }

}
