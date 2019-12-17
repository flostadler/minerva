package de.flostadler.minerva.history.listener

import de.flostadler.minerva.core.data.Timespan
import de.flostadler.minerva.core.provider.ExecutorLifeCycleProvider
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerExecutorAdded, SparkListenerExecutorRemoved}

object ExecutorLifeCycleListener {
  def apply(): ExecutorLifeCycleListener = new ExecutorLifeCycleListener()
}

class ExecutorLifeCycleListener extends SparkListener with ExecutorLifeCycleProvider {

  override def getLifeCycles: Map[String, Timespan] = lifeCycles

  var hostMapping: Map[String, String] = Map()

  private var lifeCycles: Map[String, Timespan] = Map()
  private var intermediate: Map[String, Long => Timespan] = Map()

  override def onExecutorAdded(executorAdded: SparkListenerExecutorAdded): Unit = {
    intermediate += (executorAdded.executorId -> (Timespan(executorAdded.time, _)))
    hostMapping += (executorAdded.executorId -> executorAdded.executorInfo.executorHost)
  }

  override def onExecutorRemoved(executorRemoved: SparkListenerExecutorRemoved): Unit = {
    lifeCycles += (executorRemoved.executorId -> intermediate(executorRemoved.executorId)(executorRemoved.time))
    intermediate -= executorRemoved.executorId
  }

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = if (intermediate.nonEmpty) {
      lifeCycles ++= intermediate.map(x => (x._1, x._2(applicationEnd.time)))
  }

}
