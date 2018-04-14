package integration.auth

import _root_.security.PasswordManager
import anorm._
import integration.common.{RandomHelper, TestHelper}
import org.scalatestplus.play.PlaySpec
import play.api.Logger
import play.api.db.Database
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import models.user._

object AuthTestHelper extends PlaySpec {

  def signup(body: JsValue, checkResponse: Boolean = true): WSResponse = {
    val signupResponse = TestHelper.post(s"/auth/signup", body)
    Logger.debug(s"Response for signup was: " + signupResponse.body)
    if(checkResponse) signupResponse.status mustBe OK
    signupResponse
  }

  def login(body: JsValue, checkResponse: Boolean = true): WSResponse = {
    val loginResponse = TestHelper.post(s"/auth/login", body)
    Logger.debug(s"Response for login was: " + loginResponse.body)
    if(checkResponse) loginResponse.status mustBe OK
    loginResponse
  }

  def logout(userId: Long, authToken: String, checkResponse: Boolean = true): WSResponse = {
    val logoutResponse = TestHelper.post(s"/users/$userId/logout", JsNull, "X-AuthToken" -> authToken)
    Logger.debug("Response for logout was: " + logoutResponse.body)
    if(checkResponse) logoutResponse.status mustBe OK
    logoutResponse
  }

  def createUser(email: String = RandomHelper.randomEmail,
                 pass: String = RandomHelper.randomPassword,
                 firstName: String = RandomHelper.randomFirstname): (Long, String) = {
    val signupResponse = signup(Json.obj("firstname" -> firstName, "lastname" -> "Perez", "email" -> email, "password" -> pass))
    val authToken = (signupResponse.json \ "authToken").as[String]
    val userId = (signupResponse.json \ "userId").as[Long]
    Logger.debug(s"New user created with email $email and userId $userId")
    userId -> authToken
  }

}
