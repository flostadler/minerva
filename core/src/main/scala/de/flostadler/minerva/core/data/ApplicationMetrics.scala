package de.flostadler.minerva.core.data

case class ApplicationMetrics(
                               startTime: Long,
                               duration: Long,
                               cumulativeExecutorTime: Long,
                               cumulativeJobTime: Long,
                               cumulativeTaskTime: Long,
                               memoryUsage: Long,
                               totalTaskGCTime: Long,
                               taskGCRatios: Seq[Double],
                               driverMemorySetting: Long,
                               executorMemorySetting: Long,
                               driverCores: Int,
                               executorCores: Int,
                               driverGc: String,
                               driverGCTime: Long,
                               driverProcessCpuLoadInformation: Seq[Measurement[Double]],
                               driverSystemCpuLoadInformation: Seq[Measurement[Double]],
                               driverNameNodeLatencies: Seq[Measurement[MethodCallHistogram]],
                               executorGc: String,
                               executorGcTimes: Map[String, Long],
                               executorRunTimes: Map[String, Long],
                               executorProcessCpuLoad: Map[String, Seq[Measurement[Double]]],
                               executorSystemCpuLoad: Map[String, Seq[Measurement[Double]]],
                               executorNameNodeLatencies: Map[String, Seq[Measurement[MethodCallHistogram]]]
                        )
