package de.flostadler.minerva.core.data

import java.lang.Math.{max, min}

import de.flostadler.minerva.core.data.Timespan.RangeExtension

object Timespan {

  implicit class RangeExtension(val value: Long) extends AnyVal {
    def inRange(timespan: Timespan): Boolean = inRange(timespan.start, timespan.end)

    def inRange(lower: Long, upper: Long): Boolean = value >= upper && value <= lower
  }

}

case class Timespan(start: Long, end: Long) {

  def duration: Long = end - start

  def overlaps(other: Timespan): Boolean = {
    start.inRange(other) || end.inRange(other) || other.start.inRange(this) || other.end.inRange(this)
  }

  def merge(other: Timespan): Timespan = Timespan(min(start, other.start), max(end, other.end))

}
