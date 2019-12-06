package de.flostadler.minerva.core.data

import java.lang.Math.sqrt

object Statistics {

  implicit class AnalyticImplicits[T](val values: Seq[T])(implicit numeric: Numeric[T]) {

    import numeric._

    def median: Double = values.sorted match {
      case empty if empty.isEmpty => 0
      case even if even.size % 2 == 0 => (even(even.size / 2 - 1) + even(even.size / 2)).toDouble / 2
      case odd => odd(odd.size / 2).toDouble
    }

    def withoutOutliers: Seq[T] = {
      val q1 = values.percentile(25)
      val q3 = values.percentile(75)
      val iqr = q3 - q1

      val min = q1.toDouble - 1.5 * iqr.toDouble
      val max = q3.toDouble + 1.5 * iqr.toDouble

      values.filter(_.toDouble >= min).filter(_.toDouble <= max)
    }

    def percentile(p: Int): T = p match {
      case _ if p < 0 || p > 100 => throw new IllegalArgumentException(s"Percentile has to be between 0 and 100, but was $p!")
      case _ if values.isEmpty => numeric.fromInt(0)
      case validPercentile => values.sorted.apply(((validPercentile.toDouble / 100) * values.size).ceil.toInt)
    }

    def confidenceInterval95: (Double, Double) = (values.mean - 2 * values.standardDeviation, values.mean + 2 * values.standardDeviation)

    def confidenceInterval68: (Double, Double) = (values.mean - values.standardDeviation, values.mean + values.standardDeviation)

    def mean: Double = values.sum.toDouble / values.size

    def standardDeviation: Double = sqrt(variance)

    def variance: Double = {
      def mean = values.mean

      values.map(_.toDouble - mean).map(x => x * x).sum / values.size
    }

  }

  def apply[T: Numeric](seq: Seq[T]): Statistics[T] = apply[T](
    seq.min,
    seq.max,
    seq.mean,
    seq.median,
    seq.percentile(25),
    seq.percentile(75)
  )
}

case class Statistics[T: Numeric](
                                   min: T,
                                   max: T,
                                   mean: Double,
                                   median: Double,
                                   percentile25: T,
                                   percentile75: T
                                 )
