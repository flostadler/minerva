package de.flostadler.minerva.core.provider

import de.flostadler.minerva.core.data.Timespan

trait ApplicationLifeCycleProvider {
  def getApplicationLifeCycle: Timespan

  def getAppId: String
}
