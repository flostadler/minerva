package de.flostadler.minerva.core.provider

import de.flostadler.minerva.core.data.TaskInformation

trait TaskInformationProvider {
  def getTaskInformation: Traversable[TaskInformation]
}
