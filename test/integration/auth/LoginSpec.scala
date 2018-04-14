package integration.auth

import models.auth.LoginCredentialRepo
import integration.common.{TestServer, RandomHelper, TestHelper}
import integration.user.UserTestHelper
import play.api.libs.json.Json
import play.api.libs.json.Reads._
import play.api.test.Helpers._

class LoginSpec extends TestServer {

  "Login Spec" must {

    val loginCredentialRepo = new LoginCredentialRepo(testDatabase)

    "login an existing registered user" in {
      val email = RandomHelper.randomEmail
      val password = "123456"
      AuthTestHelper.signup(Json.obj("firstname" -> "Juan", "lastname" -> "Perez", "email" -> email, "password" -> password))

      val loginResponse = AuthTestHelper.login(Json.obj("email" -> email, "password" -> password))
      val authToken = validateAndGet((loginResponse.json \ "authToken").asOpt[String])
      val userId = validateAndGet((loginResponse.json \ "userId").asOpt[Long])
      val loginCredential = validateAndGet(loginCredentialRepo.findByAuthToken(authToken))
      loginCredential.userId mustBe userId
    }

    "reject a login when email is not registered" in {
      val username = RandomHelper.randomEmail
      val password = "123456"
      val response = AuthTestHelper.login(Json.obj("firstname" -> "Juan", "lastname" -> "Perez",
        "email" -> username, "password" -> password), false)
      response.status mustBe BAD_REQUEST
      response.body mustBe Json.obj("error" -> true, "message" -> "email.notfound").toString
    }

  }
}
