package de.flostadler.minerva.history

import java.nio.file.Paths

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import de.flostadler.minerva.history.listener.EnvironmentListener
import de.flostadler.minerva.history.log.YarnLogAnalyzer
import org.scalatest.flatspec.AnyFlatSpec

import scala.io.Source
import scala.util.Success

class Foo extends AnyFlatSpec {

  "Log analysis" should "extract gc algorithm" in {
    val analyzer = resource.managed {
      Source.fromInputStream(getClass.getResourceAsStream("/g1gc_logs.txt"))
    } acquireAndGet(in => YarnLogAnalyzer(in.getLines))

    val driverGc = analyzer.getDriverGc
    val executorGc = analyzer.getExecutorGc

    val i = 0
  }

  "Analysing logs with duration profiling" should "hopefully work" in {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val rootPath = Paths.get("""/Users/florianstadler/Desktop/duration""")


    Option(rootPath.toFile)
      .map(appDir => (appDir.listFiles.find(_.getName.equalsIgnoreCase("history")).get,
        appDir.listFiles.find(_.getName.equalsIgnoreCase("logs")).get,
        appDir.getName)
      ).map(x => (Analysis.apply _).tupled(x))
      .collect { case Success(result) => result }
      .map(mapper.writeValueAsString(_))


  }

  "" should "" in {
    println(EnvironmentListener().toMB("3750m"))
  }
}
