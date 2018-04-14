package integration.auth

import com.github.nscala_time.time.Imports._
import models.auth._
import play.api.test.Helpers._
import play.api.libs.json._
import anorm.JodaParameterMetaData._
import models.auth.LoginCredentialRepo
import integration.common.{TestServer, RandomHelper}

class LogoutSpec extends TestServer {

  "Logout Spec" must {

    val loginCredentialRepo = new LoginCredentialRepo(testDatabase)

    "reject a non existing authToken when tries to logout" in {
      val dummyUserId = 123456L
      val response = AuthTestHelper.logout(dummyUserId, "dumy authToken", false)
      response.status mustBe FORBIDDEN
      response.body mustBe Json.obj("error" -> true, "message" -> "invalid.authToken").toString
    }

    "logout user" in {
      val (userId, authToken) = AuthTestHelper.createUser()
      val response = AuthTestHelper.logout(userId, authToken)
      response.body mustBe Json.obj("error" -> false, "message" -> "", "content" -> JsNull).toString
    }

    "reject a valid authToken of another user" in {
      val (userId, _) = AuthTestHelper.createUser()
      val (_, authToken) = AuthTestHelper.createUser()

      val response = AuthTestHelper.logout(userId, authToken, false)
      response.status mustBe FORBIDDEN
      response.body mustBe Json.obj("error" -> true, "message" -> "authToken.forbidden").toString
    }

    "reject a disabled authToken" in {
      val (userId, authToken) = AuthTestHelper.createUser()
      val loginCredential = validateAndGet(loginCredentialRepo.findByAuthToken(authToken))
      loginCredentialRepo.update(loginCredential.id, Seq('status -> LoginCredentialStatus.DISABLED.toString))
      val response = AuthTestHelper.logout(userId, authToken, false)
      response.status mustBe FORBIDDEN
      response.body mustBe Json.obj("error" -> true, "message" -> "invalid.authToken").toString
    }

    "reject an expired authToken" in {
      val (userId, authToken) = AuthTestHelper.createUser()
      val loginCredential = validateAndGet(loginCredentialRepo.findByAuthToken(authToken))
      loginCredentialRepo.update(loginCredential.id, Seq('expiration_date -> (DateTime.now(DateTimeZone.UTC) - 1.day)))
      val response = AuthTestHelper.logout(userId, authToken, false)
      response.status mustBe FORBIDDEN
      response.body mustBe Json.obj("error" -> true, "message" -> "invalid.authToken").toString
    }

  }
}
