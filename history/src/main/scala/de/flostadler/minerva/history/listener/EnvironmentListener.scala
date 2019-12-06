package de.flostadler.minerva.history.listener

import java.lang.Math.max

import de.flostadler.minerva.core.provider.SizingProvider
import de.flostadler.minerva.history.listener.EnvironmentListener.EnvironmentUpdateExtension
import org.apache.spark.network.util.JavaUtils.byteStringAsBytes
import org.apache.spark.scheduler.{SparkListener, SparkListenerEnvironmentUpdate}

object EnvironmentListener {

  def apply(): EnvironmentListener = new EnvironmentListener()

  implicit class EnvironmentUpdateExtension(val environmentUpdate: SparkListenerEnvironmentUpdate) extends AnyVal {
    def getProperty(key: String): Option[String] =
      environmentUpdate.environmentDetails
        .flatMap(_._2.find(_._1.equalsIgnoreCase(key)).map(_._2))
        .headOption
  }

}

class EnvironmentListener extends SparkListener with SizingProvider {

  private val MINIMUM_OVERHEAD = 384
  private val OVERHEAD_FACTOR = 0.1

  override def getExecutorCores: Int = executorCores.get

  override def getDriverCores: Int = driverCores.get

  override def getDriverMemoryMB: Long = driverMemory.get

  override def getExecutorMemoryMB: Long = executorMemory.get

  private var driverMemory: Option[Long] = _
  private var executorMemory: Option[Long] = _
  private var driverCores: Option[Int] = _
  private var executorCores: Option[Int] = _

  override def onEnvironmentUpdate(environmentUpdate: SparkListenerEnvironmentUpdate): Unit = {
    val getOverhead: String => Long => Long = key => memory => environmentUpdate.getProperty(key)
      .map(toMB)
      .getOrElse(max((memory * OVERHEAD_FACTOR).toInt, MINIMUM_OVERHEAD))

    val withOverhead: String => Long => Long = key => memory => getOverhead(key)(memory) + memory

    driverMemory = environmentUpdate.getProperty("spark.driver.memory")
      .map(toMB)
      .map(withOverhead("spark.driver.memoryOverhead")(_))

    executorMemory = environmentUpdate.getProperty("spark.executor.memory")
      .map(toMB)
      .map(withOverhead("spark.executor.memoryOverhead")(_))

    driverCores = environmentUpdate.getProperty("spark.driver.cores").map(_.toInt)
    executorCores = environmentUpdate.getProperty("spark.executor.cores").map(_.toInt)
  }

  private def toMB(str: String): Long = byteStringAsBytes(str) / 1024 / 1024
}
