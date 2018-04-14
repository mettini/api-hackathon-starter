package models

import anorm._
import play.api.db.Database
import play.api.Logger
import java.sql.Connection

trait BaseModel[T] {
  implicit val dateTimeFormatter = common.formatters.DateTimeFormatter.dateTimeFormatter

  def tableName: String
  def db: Database

  def parser: RowParser[T]

  protected def insert(arg: NamedParameter*)(implicit connection: Connection): Option[Long] = {
    val filteredArg = arg.filter{
      _.tupled match {
        case (_, pv: ParameterValue) => validatePv(pv)
        case _ => true
      }
    }
    val columnNames = " ( " ++ filteredArg.foldLeft("")((s,i) => s ++ (if(s == "") "" else ", \n") ++ i.tupled._1 ) ++ ", is_deleted) "
    val valueNames = " ( " ++ filteredArg.foldLeft("")((s,i) => s ++ (if(s == "") "" else ", \n") ++ "{" ++ i.tupled._1 ++ "}") ++ ", false ) "
    val insertQuery = s"insert into $tableName" ++ columnNames ++ "\n values " ++ valueNames
    SQL(insertQuery).on(filteredArg: _*).executeInsert()
  }

  private def validatePv(pv: ParameterValue): Boolean = {
    pv match {
      case dpv: DefaultParameterValue[_] =>
        dpv.value match {
          case None => false
          case _ => true
        }
      case _ => true
    }
  }

  def findById(id: Long): Option[T] = {
    Logger.trace(s"find by id #$id in table: $tableName")
    val selectQuery = s"SELECT * FROM $tableName WHERE id = {id} and is_deleted = false"
    db.withConnection { implicit connection =>
      SQL(selectQuery).on('id -> id).as(parser.singleOpt)
    }
  }

  def findByIdTransaction(id: Long)(implicit connection: Connection): Option[T] = {
    Logger.trace(s"find by id #$id in table: $tableName")
    SQL(s"""
      SELECT *
      FROM $tableName
      WHERE id = {id}
      AND is_deleted = false """).on('id -> id).as(parser.singleOpt)

  }

}
