package models.user.requests

import play.api.libs.json.Json

case class UserProfileRequest(
  firstname: String,
  lastname: Option[String]
)

object UserProfileRequest {
  implicit val userProfileRequestFormat = Json.format[UserProfileRequest]
}
