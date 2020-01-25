package de.flostadler.minerva.core.provider

import de.flostadler.minerva.core.data.{Measurement, MethodCallHistogram}

trait ExecutorInformationProvider {
  def getExecutorGcTimes: Map[String, Long]

  def getExecutorRunTimes: Map[String, Long]

  def getExecutorProcessCpuLoad: Map[String, Seq[Measurement[Double]]]

  def getExecutorSystemCpuLoad: Map[String, Seq[Measurement[Double]]]

  def getExecutorNameNodeLatencies: Map[String, Seq[Measurement[MethodCallHistogram]]]

  def getExecutorGc: String
}
