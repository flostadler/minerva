package de.flostadler.minerva.history

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import de.flostadler.minerva.core.analysis.MetricsAnalyzer
import org.scalatest.FlatSpec

import scala.util.Success

class Foo extends FlatSpec {

  "Analysing logs separating executors" should "be possible" in {

  }

  "Analysing logs with duration profiling" should "hopefully work" in {
    val finished = new AtomicBoolean(false)

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val rootPath = Paths.get("")

    val results = rootPath.toFile.listFiles
      .filter(_.getName.equals("duration"))
      .map(appDir => (
        appDir.listFiles.find(_.getName.equalsIgnoreCase("history")).get,
        appDir.listFiles.find(_.getName.equalsIgnoreCase("logs")).get,
        appDir.getName
      ))
      .par
      .map((Analysis.analyse _).tupled)
      .collect { case Success(s) => s }
      .map(result => MetricsAnalyzer.apply(
        result._1.applicationLifeCycleProvider,
        result._1.sizingProvider,
        result._1.jobLifeCycleProvider,
        result._1.executorLifeCycleProvider,
        result._1.taskInformationProvider,
        result._2,
        result._2
      ))

    resource.managed { new BufferedWriter(new FileWriter("")) }
      .acquireAndGet(writer => mapper.writeValue(writer, results))
  }
}
