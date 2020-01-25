package de.flostadler.minerva.core.provider

import de.flostadler.minerva.core.data.{Measurement, MethodCallHistogram}

trait DriverInformationProvider {
  def getDriverGcTime: Long

  def getDriverProcessCpuLoad: Seq[Measurement[Double]]

  def getDriverSystemCpuLoad: Seq[Measurement[Double]]

  def getDriverNameNodeLatencies: Seq[Measurement[MethodCallHistogram]]

  def getDriverGc: String
}
