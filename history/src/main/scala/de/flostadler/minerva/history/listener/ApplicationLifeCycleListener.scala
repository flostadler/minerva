package de.flostadler.minerva.history.listener

import de.flostadler.minerva.core.data.Timespan
import de.flostadler.minerva.core.provider.ApplicationLifeCycleProvider
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerApplicationStart}

object ApplicationLifeCycleListener {
  def apply(): ApplicationLifeCycleListener = new ApplicationLifeCycleListener()
}

class ApplicationLifeCycleListener extends SparkListener with ApplicationLifeCycleProvider {

  private var appStartTime: Long = _
  private var appEndTime: Long = _
  private var appId: String = _

  override def getApplicationLifeCycle: Timespan = Timespan(appStartTime, appEndTime)
  override def getAppId: String = appId

  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {
    appStartTime = applicationStart.time
    appId = applicationStart.appId.orNull
  }

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit =
    appEndTime = applicationEnd.time

}
