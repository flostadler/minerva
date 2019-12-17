package de.flostadler.minerva.history

import java.io.{File, FileInputStream}

import de.flostadler.minerva.core.analysis.MetricsAnalyzer
import de.flostadler.minerva.core.data.ApplicationMetrics
import de.flostadler.minerva.history.log.YarnLogAnalyzer

import scala.io.Source
import scala.util.{Failure, Success, Try}

object Analysis {

  def apply(history: File, logs: File, appId: String): Try[ApplicationMetrics] = {
    val logAnalyzer = resource.managed { Source.fromFile(logs) }
        .acquireAndGet(in => YarnLogAnalyzer(in.getLines))

    val replay = resource.managed { new FileInputStream(history) }
      .acquireAndGet(in => Replay(in, appId))

    if (logAnalyzer.appId.isEmpty || !logAnalyzer.appId
      .equalsIgnoreCase(replay.applicationLifeCycleProvider.getAppId)) {
      Failure(new IllegalArgumentException(s"History appId ${replay.applicationLifeCycleProvider.getAppId} and " +
        s"log appId ${logAnalyzer.appId} dont match"))
    }
    
    Success(MetricsAnalyzer.apply(
      replay.applicationLifeCycleProvider,
      replay.sizingProvider,
      replay.jobLifeCycleProvider,
      replay.executorLifeCycleProvider,
      replay.taskInformationProvider,
      logAnalyzer,
      logAnalyzer
    ))
  }

}
