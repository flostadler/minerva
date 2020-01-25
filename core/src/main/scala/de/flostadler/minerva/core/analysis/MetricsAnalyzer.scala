package de.flostadler.minerva.core.analysis

import java.lang.Math.{max, min}

import de.flostadler.minerva.core.data.{ApplicationMetrics, Timespan}
import de.flostadler.minerva.core.provider._

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object MetricsAnalyzer {
  def apply(applicationLifeCycleProvider: ApplicationLifeCycleProvider, sizingProvider: SizingProvider,
            jobLifeCycleProvider: JobLifeCycleProvider, executorLifeCycleProvider: ExecutorLifeCycleProvider,
            taskInformationProvider: TaskInformationProvider, driverInformationProvider: DriverInformationProvider,
            executorInformationProvider: ExecutorInformationProvider
           ): ApplicationMetrics = {

    val executorOccupation = {
      val executorOccupation: ListBuffer[Timespan] = ListBuffer()
      val jobTimespanQueue = mutable.Queue(jobLifeCycleProvider.getJobLifeCycles.toList.map(_._2): _*)

      @tailrec
      def mergeOverlapping(timespan: Timespan, queue: mutable.Queue[Timespan]): Timespan = {
        queue.dequeueFirst(_.overlaps(timespan)) match {
          case None => timespan
          case Some(overlap) => mergeOverlapping(timespan.merge(overlap), queue)
        }
      }

      while (jobTimespanQueue.nonEmpty) {
        executorOccupation += mergeOverlapping(jobTimespanQueue.dequeue, jobTimespanQueue)
      }

      executorOccupation.toList
    }

    val cumulativeJobTime: Long = executorOccupation
      .map(occupation => {
        executorLifeCycleProvider.getLifeCycles.toList.map(_._2)
          .filter(_.overlaps(occupation))
          .map(runtime => min(runtime.end, occupation.end) - max(runtime.start, occupation.start))
          .sum
      }).sum

    val applicationRuntime = applicationLifeCycleProvider.getApplicationLifeCycle.duration
    val cumulativeExecutorTime = executorLifeCycleProvider.getLifeCycles.map(_._2.duration).sum
    val memoryUsage: Long =
      (sizingProvider.getDriverMemoryMB * applicationRuntime + sizingProvider.getExecutorMemoryMB * cumulativeExecutorTime) / 1000

    ApplicationMetrics(
      startTime = applicationLifeCycleProvider.getApplicationLifeCycle.start,
      duration = applicationRuntime,
      cumulativeExecutorTime = cumulativeExecutorTime,
      cumulativeJobTime = cumulativeJobTime,
      cumulativeTaskTime = taskInformationProvider.getTaskInformation.map(_.duration).sum,
      memoryUsage = memoryUsage,
      driverMemorySetting = sizingProvider.getDriverMemorySetting,
      executorMemorySetting = sizingProvider.getExecutorMemorySetting,
      driverCores = sizingProvider.getDriverCores,
      executorCores = sizingProvider.getExecutorCores,
      driverGc = driverInformationProvider.getDriverGc,
      executorGc = executorInformationProvider.getExecutorGc,
      totalTaskGCTime = taskInformationProvider.getTaskInformation.map(_.gcTime).sum,
      taskGCRatios = taskInformationProvider.getTaskInformation.map(x => x.gcTime.toDouble / x.duration).toSeq,
      driverGCTime = driverInformationProvider.getDriverGcTime,
      driverProcessCpuLoadInformation = driverInformationProvider.getDriverProcessCpuLoad,
      driverSystemCpuLoadInformation = driverInformationProvider.getDriverSystemCpuLoad,
      driverNameNodeLatencies = driverInformationProvider.getDriverNameNodeLatencies,
      executorGcTimes = executorInformationProvider.getExecutorGcTimes,
      executorRunTimes = executorInformationProvider.getExecutorRunTimes,
      executorProcessCpuLoad = executorInformationProvider.getExecutorProcessCpuLoad,
      executorSystemCpuLoad = executorInformationProvider.getExecutorSystemCpuLoad,
      executorNameNodeLatencies = executorInformationProvider.getExecutorNameNodeLatencies
    )
  }
}
