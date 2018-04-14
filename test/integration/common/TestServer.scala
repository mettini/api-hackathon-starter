package integration.common

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatest._
import org.scalatestplus.play._

import play.api._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._

import play.api.test._
import play.api.test.Helpers.{ GET => GET_REQUEST, _ }

import play.api.inject.guice._
import play.api.routing._
import play.api.routing.sird._

trait TestServer extends PlaySpec with GuiceOneServerPerSuite with TestLoaderComponents {

  val customTestConf: Map[String, AnyRef] = Map (
    "email.enabled" -> false.asInstanceOf[AnyRef],
    "db.default.url" -> "jdbc:h2:mem:play;MODE=MYSQL;DB_CLOSE_DELAY=-1",
    "db.default.driver" -> "org.h2.Driver"
  )

  val customTestRoutes: PartialFunction[(String, String), play.api.mvc.Handler] = {
    case ("GET", "/get-200") => Action(Results.Ok(("ok")))
  }

  override implicit lazy val app: Application = buildTestApp(customTestRoutes, customTestConf)

  def validateAndGet[T](optional: Option[T]): T = {
    optional mustBe defined
    optional.get
  }

  def validateAndGet[T](optional: Option[T], clue: String): T = {
    withClue(clue) { optional mustBe defined }
    optional.get
  }

}
