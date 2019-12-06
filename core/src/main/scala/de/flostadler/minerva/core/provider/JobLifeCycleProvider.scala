package de.flostadler.minerva.core.provider

import de.flostadler.minerva.core.data.Timespan

trait JobLifeCycleProvider {
  def getJobLifeCycles: Map[Int, Timespan]
}
