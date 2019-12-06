package de.flostadler.minerva.history

import java.io.{File, FileInputStream, InputStream}

import de.flostadler.minerva.core.provider.{ApplicationLifeCycleProvider, ExecutorLifeCycleProvider, JobLifeCycleProvider, SizingProvider, TaskInformationProvider}
import de.flostadler.minerva.history.listener.{ApplicationLifeCycleListener, EnvironmentListener, ExecutorLifeCycleListener, JobListener, TaskListener}
import org.apache.spark.deploy.history.DataCollection

object Replay {
  def apply(in: InputStream, sourceName: String): Replay = new Replay(in, sourceName)

  def apply(file: File, sourceName: String): Replay = resource.managed { new FileInputStream(file) }
    .acquireAndGet(in => Replay(in, sourceName))
}

class Replay(in: InputStream, sourceName: String) {

  private val applicationLifeCycleListener: ApplicationLifeCycleListener = ApplicationLifeCycleListener()
  private val executorLifeCycleListener: ExecutorLifeCycleListener = ExecutorLifeCycleListener()
  private val jobListener: JobListener = JobListener()
  private val environmentListener: EnvironmentListener = EnvironmentListener()
  private val taskListener: TaskListener = TaskListener()

  val applicationLifeCycleProvider: ApplicationLifeCycleProvider = applicationLifeCycleListener
  val executorLifeCycleProvider: ExecutorLifeCycleProvider = executorLifeCycleListener
  val jobLifeCycleProvider: JobLifeCycleProvider = jobListener
  val sizingProvider: SizingProvider = environmentListener
  val taskInformationProvider: TaskInformationProvider = taskListener


  DataCollection().replay(in, sourceName, List(
    applicationLifeCycleListener,
    executorLifeCycleListener,
    jobListener,
    environmentListener,
    taskListener
  ))

}
