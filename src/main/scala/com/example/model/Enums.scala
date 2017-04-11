package com.example.model

import org.joda.time.DateTimeConstants



object DayOfTheWeek {

  sealed trait EnumValue extends Serializable

  case object NO_WEEK_DAYS extends EnumValue

  case object MONDAY extends EnumValue

  case object TUESDAY extends EnumValue

  case object WEDNESDAY extends EnumValue

  case object THURSDAY extends EnumValue

  case object FRIDAY extends EnumValue

  case object SATURDAY extends EnumValue

  case object SUNDAY extends EnumValue

  val days = Seq(NO_WEEK_DAYS, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

  val dayMap = Map(
    DateTimeConstants.MONDAY -> MONDAY,
    DateTimeConstants.TUESDAY -> TUESDAY,
    DateTimeConstants.WEDNESDAY -> WEDNESDAY,
    DateTimeConstants.THURSDAY -> THURSDAY,
    DateTimeConstants.FRIDAY -> FRIDAY,
    DateTimeConstants.SATURDAY -> SATURDAY,
    DateTimeConstants.SUNDAY -> SUNDAY)

  val numberOfWeekDays = 7

}

