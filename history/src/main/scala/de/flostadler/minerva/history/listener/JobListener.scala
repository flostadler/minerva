package de.flostadler.minerva.history.listener

import de.flostadler.minerva.core.data.Timespan
import de.flostadler.minerva.core.provider.JobLifeCycleProvider
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerJobEnd, SparkListenerJobStart}

object JobListener {
  def apply(): JobListener = new JobListener()
}

class JobListener extends SparkListener with JobLifeCycleProvider {

  override def getJobLifeCycles: Map[Int, Timespan] = lifeCycles

  private var lifeCycles: Map[Int, Timespan] = Map()
  private var intermediate: Map[Int, Long => Timespan] = Map()

  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    intermediate += (jobStart.jobId -> (Timespan(jobStart.time, _)))
  }

  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    lifeCycles += (jobEnd.jobId -> intermediate(jobEnd.jobId)(jobEnd.time))
    intermediate -= jobEnd.jobId
  }

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
    if (intermediate.nonEmpty) {
      println(s"WARN! No Job-End for ${intermediate.keys.mkString(", ")}")

      lifeCycles ++= intermediate.map(x => (x._1, x._2(applicationEnd.time)))
    }
  }
}
