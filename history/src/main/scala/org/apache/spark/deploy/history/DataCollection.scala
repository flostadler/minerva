package org.apache.spark.deploy.history

import java.io.InputStream

import org.apache.spark.scheduler.ReplayListenerBus.ReplayEventsFilter
import org.apache.spark.scheduler.{ReplayListenerBus, SparkListener}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse

object DataCollection {
  val events = Set(
    "SparkListenerTaskEnd",
    "SparkListenerApplicationStart",
    "SparkListenerApplicationEnd",
    "SparkListenerExecutorAdded",
    "SparkListenerExecutorRemoved",
    "SparkListenerJobStart",
    "SparkListenerJobEnd",
    "SparkListenerEnvironmentUpdate"
  )

  def apply(in: InputStream, sourceName: String, listeners: Traversable[SparkListener]): Unit = {
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

    val replayBus = new ReplayListenerBus()
    listeners.foreach(replayBus.addListener)

    val filter: ReplayEventsFilter = e => events.contains(parse(e).extract[Map[String, Any]].apply("Event").asInstanceOf[String])

    replayBus.replay(in, sourceName, maybeTruncated = false, eventsFilter = filter)
  }
}
