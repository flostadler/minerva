package de.flostadler.minerva.core.provider

trait SizingProvider {
  def getExecutorCores: Int

  def getDriverCores: Int

  def getDriverMemoryMB: Long

  def getExecutorMemoryMB: Long

  def getDriverMemorySetting: Long

  def getExecutorMemorySetting: Long
}
