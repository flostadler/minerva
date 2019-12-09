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
                               driverGCTime: Long,
                               driverProcessCpuLoadInformation: Seq[Measurement[Double]],
                               driverSystemCpuLoadInformation: Seq[Measurement[Double]],
                               driverNameNodeLatencies: Seq[Measurement[MethodCallHistogram]],
                               executorGcTimes: Map[String, Long],
                               executorProcessCpuLoad: Map[String, Seq[Measurement[Double]]],
                               executorSystemCpuLoad: Map[String, Seq[Measurement[Double]]],
                               executorNameNodeLatencies: Map[String, Seq[Measurement[MethodCallHistogram]]]
                        )
