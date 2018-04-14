package integration.common

import _root_.models.user._
import anorm._
import play.api.Configuration
import org.scalatestplus.play._
import play.api._
import play.api.db.Database
import play.api.libs.json._
import play.api.libs.ws._
import play.api.test.Helpers._
import play.api.test.WsTestClient

object TestHelper extends PlaySpec {

  lazy val port = "19001"
  implicit lazy val portImpl = new play.api.http.Port(port.toInt)

  def updateUserProfile(userId: Long, body: JsValue, authToken: String, checkResponse: Boolean = true): WSResponse = {
    val updateUserProfileResult = put(s"/users/$userId/profile", body, "X-AuthToken" -> authToken)
    Logger.debug("Response for updateUserProfile was: " + updateUserProfileResult.body)
    if(checkResponse) updateUserProfileResult.status mustBe OK
    updateUserProfileResult
  }

  def verifyEmail(hash: String, checkResponse: Boolean = true): WSResponse = {
    val activateEmailResult = get(s"/email-verification/$hash")
    Logger.debug("Response for verify email was: " + activateEmailResult.body)
    if(checkResponse) activateEmailResult.status mustBe OK
    activateEmailResult
  }

  def resendEmailVerification(authToken: String, userId: Long, body: JsValue): WSResponse = {
    val resendEmailResult = post(s"/users/$userId/email-verification", body, "X-AuthToken" -> authToken)
    Logger.debug("Response for resend email was: " + resendEmailResult.body)
    resendEmailResult
  }

  def resetPass(body: JsValue, checkResponse: Boolean = true): WSResponse = {
    val resetPassResult = post(s"/password-change/request", body)
    Logger.debug("Response for reset pass was: " + resetPassResult.body)
    if(checkResponse) resetPassResult.status mustBe OK
    resetPassResult
  }

  def changePass(body: JsValue, checkResponse: Boolean = true): WSResponse = {
    val changePassResult = post(s"/password-change/request", body)
    Logger.debug("Response for change pass was: " + changePassResult.body)
    if(checkResponse) changePassResult.status mustBe OK
    changePassResult
  }

  def deleteUser(userId: String, authToken: String): WSResponse = {
    val deleteUserResponse = delete(s"/users/$userId", "X-AuthToken" -> authToken)
    Logger.debug("Response for delete user was: " + deleteUserResponse.body)
    deleteUserResponse
  }

  def findByIdNoDeleted(id: Long)(implicit db: Database): Option[User] = {
    Logger.trace(s"find by id #$id in users")
     val userRepo = UserRepo(UserConfigRepo(db), db)
    val selectQuery = s" SELECT * FROM users WHERE id = {id} "
    db.withConnection { implicit connection =>
      SQL(selectQuery).on('id -> id).as(userRepo.parser.singleOpt)
    }
  }

  def findEmailVerificationById(id: Long)(implicit db: Database): Option[EmailVerification] = {
    Logger.trace(s"find by id #$id in emailVerifications")
    val emailVerificationRepo = new EmailVerificationRepo(db)
    val selectQuery = s" SELECT * FROM email_verifications WHERE id = {id} "
    db.withConnection { implicit connection =>
      SQL(selectQuery).on('id -> id).as(emailVerificationRepo.parser.singleOpt)
    }
  }

  def put(url: String, body: JsValue, hdrs: (String, String)*): WSResponse = WsTestClient.withClient { client =>
    val wsRequest = client.url(url)
      .addHttpHeaders(hdrs: _*)
      .addHttpHeaders("X-Platform" -> "web", "X-Source" -> "source", "X-Ip" -> "127.0.0.1", "X-Client-Id" -> "tester")
    await(wsRequest.put(body))
  }

  def post(url: String, body: JsValue, hdrs: (String, String)*): WSResponse = WsTestClient.withClient { client =>
    val wsRequest = client.url(url)
      .addHttpHeaders(hdrs: _*)
      .addHttpHeaders("X-Platform" -> "web", "X-Source" -> "source", "X-Ip" -> "127.0.0.1", "X-Client-Id" -> "tester")
    await(wsRequest.post(body))
  }

  def get(url: String, hdrs: (String, String)*): WSResponse = WsTestClient.withClient { client =>
    val wsRequest = client.url(url)
      .addHttpHeaders(hdrs: _*)
      .addHttpHeaders("X-Platform" -> "web", "X-Source" -> "source", "X-Ip" -> "127.0.0.1", "X-Client-Id" -> "tester")
    await(wsRequest.get())
  }

  def delete(url: String, hdrs: (String, String)*): WSResponse = WsTestClient.withClient { client =>
    val wsRequest = client.url(url)
      .addHttpHeaders(hdrs: _*)
      .addHttpHeaders("X-Platform" -> "web", "X-Source" -> "source", "X-Ip" -> "127.0.0.1", "X-Client-Id" -> "tester")
    await(wsRequest.delete())
  }

  def validateAndGet[T](optional: Option[T]): T = {
    optional mustBe defined
    optional.get
  }

  def validateAndGet[L,R](either: Either[L,R]): R = {
    validateAndGet(either.right.toOption)
  }
}
