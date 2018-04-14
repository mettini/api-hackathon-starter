package integration.user

import anorm.JodaParameterMetaData._
import anorm._
import play.api.Configuration
import com.github.nscala_time.time.Imports._
import integration.common.TestHelper
import org.scalatestplus.play._
import play.api._
import play.api.db.Database
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._


object UserTestHelper extends PlaySpec {

  def getUser(userId: Long, authToken: String, checkResponse: Boolean = true): WSResponse = {
    val response = TestHelper.get(s"/users/$userId", "X-AuthToken" -> authToken)
    Logger.debug("Response for get user was: " + response.body)
    if(checkResponse) response.status mustBe OK
    response
  }

}
