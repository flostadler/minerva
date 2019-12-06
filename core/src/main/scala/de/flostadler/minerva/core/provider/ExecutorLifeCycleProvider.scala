package de.flostadler.minerva.core.provider

import de.flostadler.minerva.core.data.Timespan

trait ExecutorLifeCycleProvider {
  def getLifeCycles: Map[String, Timespan]
}
