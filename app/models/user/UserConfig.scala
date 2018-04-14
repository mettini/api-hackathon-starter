package models.user

import java.sql.Connection

import anorm._
import anorm.SqlParser._
import anorm.JodaParameterMetaData._
import com.github.nscala_time.time.Imports._
import play.api.Logger
import play.api.db.Database

import models.BaseModel

case class UserConfig(
  isTest: Boolean,
  isModerated: Boolean
)

case class UserConfigRepo(val db: Database) extends BaseModel[UserConfig] {

  override def parser: RowParser[UserConfig] = {
    bool("is_test") ~
    bool("is_moderated") map { case
      isTest~
      isModerated =>
        UserConfig(
          isTest,
          isModerated
        )
    }
  }

  override def tableName: String = "user_configs"

  val defaultConfig = UserConfig(isTest = false, isModerated = false)

  def getUserConfig(userId: Long): UserConfig = {
    db.withConnection { implicit connection =>
      SQL(s"""
        SELECT *
        FROM $tableName
        WHERE user_id = {userId}
        AND is_deleted = false
          """).on('userId -> userId).as(parser.singleOpt)
    } getOrElse defaultConfig
  }

  def getUserConfigOpt(userId: Long)(implicit connection: Connection): Option[UserConfig] = {
    SQL(s"""
      SELECT *
      FROM $tableName
      WHERE user_id = {userId}
      AND is_deleted = false
        """).on('userId -> userId).as(parser.singleOpt)
  }

  def save(userId: Long,
           isTest: Boolean = false,
           isModerated: Boolean = false)(implicit connection: Connection): Either[String, Long] = {
    Logger.debug(
      s"""User config save userId: $userId isTest: $isTest isModerated: $isModerated""".stripMargin)
      insert(
        "user_id" -> userId,
        "is_test" -> isTest,
        "is_moderated" -> isModerated,
        "creation_date" -> DateTime.now(DateTimeZone.UTC),
        "last_modification_date" -> DateTime.now(DateTimeZone.UTC)
      ).toRight("there was an error while creating the user config")
  }

  def setTestConfig(userId: Long, isTest: Boolean): Either[String, Long] =
    db.withTransaction { implicit connection =>
      getUserConfigOpt(userId).fold {
        save(userId, isTest, false)
      } { userConfig =>
        update(userId, Seq(NamedParameter("is_test", isTest)))
      }
    }

  def setModeratedConfig(userId: Long, isModerated: Boolean): Either[String, Long] =
    db.withTransaction { implicit connection =>
      getUserConfigOpt(userId).fold {
        save(userId, false, isModerated)
      } { userConfig =>
        update(userId, Seq(NamedParameter("is_moderated", isModerated)))
      }
    }

  private def update(userId: Long, o: Seq[NamedParameter])(implicit connection: Connection): Either[String, Long] = {
    val updateSQL = s"update $tableName set " + o.map(k => s"${k.name}={${k.name}}").mkString(",") +
      ", last_modification_date = NOW() where user_id = {userId} "
    SQL(updateSQL).on(o :+ NamedParameter("userId", userId): _*).executeUpdate() match {
      case 1 => Right(1)
      case 0 => Left(s"there was an error updating the user config #$userId for parameters ${o.toString}")
    }
  }

}
