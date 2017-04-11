package com.example.utilities.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.example.utilities.ActorsProtocol.{BookList, BookPayload}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, LocalDate}
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val bookFormat = jsonFormat7(BookPayload)
  implicit val bookListFormat = jsonFormat1(BookList)

  implicit object BookListJsonFormat extends RootJsonFormat[BookList] {
    def read(value: JsValue) = BookList(value.convertTo[List[BookPayload]])
    def write(f: BookList) = ???
  }

  implicit object LocalDateFormat extends RootJsonFormat[LocalDate] {

    val formatter = ISODateTimeFormat.basicDateTimeNoMillis

    def write(obj: LocalDate): JsValue = {
      JsString(formatter.print(obj))
    }

    def read(json: JsValue): LocalDate = json match {
      case JsString(s) => try {
        formatter.parseLocalDate(s)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): LocalDate = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

    val formatter = ISODateTimeFormat.basicDateTimeNoMillis

    def write(obj: DateTime): JsValue = {
      JsString(formatter.print(obj))
    }

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => try {
        formatter.parseDateTime(s)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }

}
