package de.flostadler.minerva.history

import java.nio.file.Paths

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import de.flostadler.minerva.core.analysis.MetricsAnalyzer
import org.scalatest.FlatSpec

import scala.util.Success

class Foo extends FlatSpec {

  "Analysing logs separating executors" should "be possible" in {

  }

  "Analysing logs with duration profiling" should "hopefully work" in {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val rootPath = Paths.get("/Users/florianstadler/Desktop")

    val evaluation = rootPath.toFile.listFiles
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
      )).map(mapper.writeValueAsString)
    evaluation
  }

  "mapper" should "deserialize application metrics" in {

  }

  //  "Analysing history files" should "give reasonable results" in {
  //    val dataDirectory = Paths.get("/Users/florianstadler/development/minerva/data").toFile
  //
  //    val resourceUsages = dataDirectory.listFiles
  //      .map(appDir => (
  //        appDir.listFiles.find(_.getName.equalsIgnoreCase("history")).get,
  //        appDir.listFiles.find(_.getName.equalsIgnoreCase("logs")).get,
  //        appDir.getName
  //      ))
  //      .par
  //      .map((Analysis.analyse _).tupled)
  //      .map(_.get)
  //      .map(res => (
  //        res._1.applicationLifeCycleProvider,
  //        res._1.sizingProvider,
  //        res._1.jobLifeCycleProvider,
  //        res._1.executorLifeCycleProvider,
  //        res._1.taskInformationProvider,
  //        res._2
  //      ))
  //      .map((ResourceUsageAnalyzer.apply _).tupled.andThen(_.getResourceUsage))
  //      .toList
  //
  //    val runtimes = resourceUsages.map(_.applicationRuntime)
  //
  //    val size = runtimes.size
  //    val withoutOutliers = runtimes.withoutOutliers
  //
  //    val withoutSize = withoutOutliers.size
  //
  //    val runtimeStatistics = Statistics(withoutOutliers)
  //    val variance = withoutOutliers.variance
  //    val sd = withoutOutliers.standardDeviation
  //
  //    val integralVariance = variance.toLong
  //    val integralSd = sd.toLong
  //
  //    def toMinute: Double => Double = a => a / 60000
  //
  //    val confidenceInterval95 = withoutOutliers.confidenceInterval95
  //    val confidenceInterval68 = withoutOutliers.confidenceInterval68
  //
  //    val conf68 = f"68: [${toMinute(confidenceInterval68._1)}%.2f; ${toMinute(confidenceInterval68._2)}%.2f]"
  //    val conf95 = f"95: [${toMinute(confidenceInterval95._1)}%.2f; ${toMinute(confidenceInterval95._2)}%.2f]"
  //
  //
  //    val list = withoutOutliers.foldLeft("")(_ + "\n" + _.toString)
  //    val i = 0
  //
  //    //confidence interval 95%: [1479440.2; 2186348.2]
  //    //[24.65; 36.439]
  //  }
}
