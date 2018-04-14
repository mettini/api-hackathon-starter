package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import services.auth.SessionService
import models.user.UserRepo
import common.actions.RequestType
import common.actions.AuthActions._

abstract class AbstractController(sessionService: SessionService, 
                                  userRepo: UserRepo,
                                  cc: ControllerComponents) extends play.api.mvc.AbstractController(cc) {

  def badRequest(msg: String): Result = {
    Logger.debug(s"BadRequest: error '$msg'")
    BadRequest(Json.obj("error" -> true, "message" -> msg))
    // TODO: manejar el requestType Web / Api
  }

  def error(msg: String): Result = {
    Logger.debug(s"Error: '$msg'")
    InternalServerError(Json.obj("error" -> true, "message" -> msg))
    // TODO: manejar el requestType Web / Api
  }

  def ok(json: JsValue): Result = {
    Ok(Json.obj("error" -> false, "message" -> "", "content" -> json))
  }

  def ok(message: String, json: JsValue): Result = {
    Ok(Json.obj("error" -> false, "message" -> message, "content" -> json))
  }

  def obtainJsonField[T](field: String, json: JsValue)(implicit rds: Reads[T]): Either[String, T] = {
    (json \ field).validate[T] match {
      case s: JsSuccess[T] => Right(s.get)
      case e: JsError => Left("invalid." + field)
    }
  }

  def obtainJsonNullableField[T](field: String, json: JsValue)(implicit rds: Reads[T]): Option[T] = {
    (json \ field).validate[T] match {
      case s: JsSuccess[T] => Some(s.get)
      case e: JsError => None
    }
  }

  val LoggedAction = { (userId: Long, requestType: RequestType) =>
    Action andThen Logged(userId, sessionService, requestType, userRepo)
  }

  val MaybeLoggedAction = { (requestType: RequestType) =>
    Action andThen MaybeLogged(sessionService, requestType, userRepo)
  }

}
