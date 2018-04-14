package di

import play.api.i18n.Langs
import play.api.mvc.ControllerComponents
import services.ServicesModule

trait AppModule extends ServicesModule {

  import com.softwaremill.macwire._
  import controllers.PingController
  import controllers.user.{ChangePasswordController, EmailVerificationController, UserController, UserProfileController}
  import controllers.auth.{LoginController, LogoutController, SignupController}

  lazy val pingController = wire[PingController]
  lazy val loginController = wire[LoginController]
  lazy val logoutController = wire[LogoutController]
  lazy val signupController = wire[SignupController]
  lazy val changePasswordController = wire[ChangePasswordController]
  lazy val emailVerificationController = wire[EmailVerificationController]
  lazy val userController = wire[UserController]
  lazy val userProfileController = wire[UserProfileController]

  def langs: Langs

  def controllerComponents: ControllerComponents
}
