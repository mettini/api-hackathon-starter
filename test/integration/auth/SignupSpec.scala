package integration.auth

import models.auth._
import integration.common.{TestServer, RandomHelper, TestHelper}
import play.api.libs.json.Json
import play.api.test.Helpers._
import models.user._

class SignupSpec extends TestServer {

  "Signup Spec" must {

    val userRepo = UserRepo(UserConfigRepo(testDatabase), testDatabase)
    val userProfileRepo = new UserProfileRepo(testDatabase)
    val loginCredentialRepo = new LoginCredentialRepo(testDatabase)

    "register a new user" in {
      val (userId, authToken) = AuthTestHelper.createUser()
      val loginCredential = validateAndGet(loginCredentialRepo.findByAuthToken(authToken))
      loginCredential.userId mustBe userId
      userRepo.findById(loginCredential.userId) mustBe defined
      userProfileRepo.findByUserId(loginCredential.userId) mustBe defined
    }

    "reject a new user without firstname" in {
      val email = RandomHelper.randomEmail
      val response = AuthTestHelper.signup(Json.obj("lastname" -> "Perez", "email" -> email, "password" -> "123456"), false)
      response.status mustBe BAD_REQUEST
      response.body mustBe Json.obj("error" -> true, "message" -> "invalid.firstname").toString
    }

    "reject a new user without lastname" in {
      val email = RandomHelper.randomEmail
      val response = AuthTestHelper.signup(Json.obj("firstname" -> "Juan", "email" -> email, "password" -> "123456"), false)
      response.status mustBe BAD_REQUEST
      response.body mustBe Json.obj("error" -> true, "message" -> "invalid.lastname").toString
    }

    "reject a new user without email" in {
      val response = AuthTestHelper.signup(Json.obj("firstname" -> "Juan", "lastname" -> "Perez", "password" -> "123456"), false)
      response.status mustBe BAD_REQUEST
      response.body mustBe Json.obj("error" -> true, "message" -> "invalid.email").toString
    }

    "reject a new user with invalid email" in {
      val response = AuthTestHelper.signup(Json.obj("firstname" -> "Juan", "lastname" -> "Perez", "email" -> "cacho_gmail.com",
        "password" -> "123456"), false)
      response.status mustBe BAD_REQUEST
      response.body mustBe Json.obj("error" -> true, "message" -> "invalid.email").toString
    }

    "reject a new user with an already used email" in {
      val email = RandomHelper.randomEmail
      val firstResponse = AuthTestHelper.signup(Json.obj("firstname" -> "Juan", "lastname" -> "Perez", "email" -> email,
        "password" -> "123456"))
      val authToken = validateAndGet((firstResponse.json \ "authToken").asOpt[String])
      val userId = validateAndGet((firstResponse.json \ "userId").asOpt[Long])
      val loginCredential = validateAndGet(loginCredentialRepo.findByAuthToken(authToken))
      loginCredential.userId mustBe userId
      userRepo.findById(loginCredential.userId) mustBe defined

      val secondResponse = AuthTestHelper.signup(Json.obj("firstname" -> "Juan", "lastname" -> "Perez", "email" -> email,
        "password" -> "123456"), false)
      secondResponse.status mustBe BAD_REQUEST
      secondResponse.body mustBe Json.obj("error" -> true, "message" -> "email.already.used").toString
    }

  }

}
