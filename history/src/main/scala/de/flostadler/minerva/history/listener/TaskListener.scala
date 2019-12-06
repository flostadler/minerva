package de.flostadler.minerva.history.listener

import de.flostadler.minerva.core.data.TaskInformation
import de.flostadler.minerva.core.provider.TaskInformationProvider
import org.apache.spark.scheduler.{SparkListener, SparkListenerTaskEnd}

import scala.collection.mutable.ListBuffer

object TaskListener {
  def apply(): TaskListener = new TaskListener()
}

class TaskListener extends SparkListener with TaskInformationProvider {

  override def getTaskInformation: Traversable[TaskInformation] = taskInformation.toList

  private val taskInformation: ListBuffer[TaskInformation] = ListBuffer()

  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = taskInformation += TaskInformation(
      taskEnd.taskInfo.executorId,
      taskEnd.taskInfo.duration,
      taskEnd.taskMetrics.jvmGCTime
    )

}
