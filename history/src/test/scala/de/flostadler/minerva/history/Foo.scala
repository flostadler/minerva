package de.flostadler.minerva.history

import java.nio.file.Paths

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import de.flostadler.minerva.history.listener.EnvironmentListener
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Success

class Foo extends AnyFlatSpec {

  "Analysing logs separating executors" should "be possible" in {

  }

  "Analysing logs with duration profiling" should "hopefully work" in {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val rootPath = Paths.get("""C:\Users\f.stadler\Desktop\new_data""")


    rootPath.toFile.listFiles.toList.take(1)
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
