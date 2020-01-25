package de.flostadler.minerva.cmd

import java.io.{BufferedWriter, File, FileFilter, FileWriter}
import java.nio.file.Paths

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import de.flostadler.minerva.history.Analysis
import org.rogach.scallop._

import scala.util.Failure

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val replace = opt[Boolean]()
  val multiple = opt[Boolean]()
  val directory = opt[String]()
  val history = opt[String](required = true)
  val log = opt[String](required = true)
  val out = opt[String](required = true)
  verify()
}

object Command {
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)

    if (conf.multiple()) {
      analyseMultiple(conf)
    } else {
      val history = new File(conf.history())
      val log = new File(conf.log())
      val out = new File(conf.out())

      if (!history.exists) {
        println("History file doesn't exist!")
        System.exit(-1)
      }
      if (!log.exists) {
        println("Log file doesn't exist!")
        System.exit(-1)
      }
      analyse(history, log, out, "spark-application")
    }
  }

  private def analyse(history: File, log: File, out: File, name: String): Unit = {
    Analysis(history, log, name)
      .recoverWith { case e => println(s"Error during analysis: ${e.getMessage}"); Failure(e) }
      .foreach(metrics => resource.managed(new BufferedWriter(new FileWriter(out)))
        .acquireAndGet(writer => mapper.writeValue(writer, metrics))
      )
  }

  private def analyseMultiple(conf: Conf): Unit = {
    val baseDir = new File(conf.directory())

    if (!baseDir.exists) {
      println("Base directory doesn't exist!")
      System.exit(-1)
    }
    if (!baseDir.isDirectory) {
      println("Base directory is no directory!")
      System.exit(-1)
    }

    def fileFilter(name: String) = new FileFilter {
      override def accept(pathname: File): Boolean = pathname.getName.equals(name)
    }

    val dirFilter: FileFilter = new FileFilter {
      override def accept(pathname: File): Boolean = pathname.exists && pathname.isDirectory &&
        pathname.listFiles(fileFilter(conf.history())).nonEmpty &&
        pathname.listFiles(fileFilter(conf.log())).nonEmpty &&
        (conf.replace() || pathname.listFiles(fileFilter(conf.out())).isEmpty)
    }

    val dirs = baseDir.listFiles(dirFilter)

    resource.managed { EnhancedProgressBar(dirs.size) }
      .acquireAndGet(progressBar => dirs.par
            .map(dir => (Paths.get(dir.getPath, conf.history()).toFile,
              Paths.get(dir.getPath, conf.log()).toFile,
              Paths.get(dir.getPath, conf.out()).toFile, dir.getName)
            ).map(x => progressBar execute { _ => (analyse _).tupled(x) })
      )
  }
}
