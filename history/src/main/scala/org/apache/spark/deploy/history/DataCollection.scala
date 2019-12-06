package org.apache.spark.deploy.history

import java.io.InputStream

import org.apache.spark.scheduler.{ReplayListenerBus, SparkListener}

object DataCollection {
  def apply(): DataCollection = new DataCollection()
}

class DataCollection {
  def replay(in: InputStream, sourceName: String, listeners: Traversable[SparkListener]): Unit = {
    val replayBus = new ReplayListenerBus()
    listeners.foreach(replayBus.addListener)
    replayBus.replay(in, sourceName, maybeTruncated = false)
  }
}
