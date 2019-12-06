package de.flostadler.minerva.history.log

import de.flostadler.minerva.core.data.{Measurement, MethodCallHistogram}
import de.flostadler.minerva.core.provider.{DriverInformationProvider, ExecutorInformationProvider}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import de.flostadler.minerva.history.log.YarnLogAnalyzer.CpuMeasurementExtensions
import de.flostadler.minerva.history.log.YarnLogAnalyzer.LogAnalyzerExtensions

object YarnLogAnalyzer {
  private val ReportPattern = "^ConsoleOutputReporter - ([^:]*): (.*)$".r

  def apply(logs: Iterator[String]): YarnLogAnalyzer = {
    val agentReports: Seq[(String, Map[String, Any])] = {
      implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

      logs.map {
        case ReportPattern(name, obj) => Some(name, obj)
        case _ => None
      }.filter(_.isDefined)
        .map(_.get)
        .filter(!_._2.isEmpty)
        .map(x => (x._1, parse(x._2).extract[Map[String, Any]]))
      }.toList

    new YarnLogAnalyzer(agentReports)
  }

  implicit class LogAnalyzerExtensions(val events: Seq[(String, Map[String, Any])]) extends AnyVal {
    def getMetrics(measurement: String): Seq[Map[String, Any]] = events.filter(_._1.equals(measurement)).map(_._2)

    def getMetrics(role: String, measurement: String): Seq[Map[String, Any]] = events.filter(_._2("role").equals(role))
      .getMetrics(measurement)

    def getMeasurement[A](measurement: String)(key: String): Seq[Measurement[A]] = events.getMetrics(measurement).getMeasurement(key)

    def getMeasurement[A](role: String, measurement: String)(key: String): Seq[Measurement[A]] = events.getMetrics(role, measurement).getMeasurement(key)

    def getAppId: String = getMetrics("ProcessInfo").get[String]("appId").head
  }

  implicit class CpuMeasurementExtensions(val events: Seq[Map[String, Any]]) extends AnyVal {

    def get[A](key: String): Seq[A] = events.flatMap(_.get(key)).map(_.asInstanceOf[A])

    def getMeasurement[A](key: String): Seq[Measurement[A]] = events.map(x => (x("epochMillis").asInstanceOf[BigInt].longValue, x.get(key)))
      .filter(_._2.isDefined)
      .map(x => Measurement(x._1, x._2.get.asInstanceOf[A]))

    def getMatching[A](key: String, value: String)(extractionKey: String): Seq[A] = events.filter(_(key).toString.equalsIgnoreCase(value)).map(_(extractionKey).asInstanceOf[A])

    def gcTime: Long = events.maxBy(_.getOrElse("epochMillis", BigInt(-1)).asInstanceOf[BigInt].longValue)
        .get("gc")
        .map {
          case multiGC: Seq[Map[String, Any]] => multiGC
          case singleGC: Map[String, Any] => List(singleGC)
          case _ => throw new UnsupportedOperationException("Found unknown gc construct!")
        }
        .map(x => x.flatMap(_.get("collectionTime")).map(_.asInstanceOf[BigInt].longValue).sum)
        .getOrElse(0)

    def nameNodeLatency: Seq[Measurement[MethodCallHistogram]] = events
      .filter(_("className").toString.equalsIgnoreCase("org.apache.hadoop.hdfs.protocolPB.ClientNamenodeProtocolTranslatorPB"))
      .groupBy(x => (x("epochMillis"), x("className"), x("methodName")))
      .map {
        case ((timestamp: BigInt, className: String, methodName: String), metrics: Seq[Map[String, Any]]) => Measurement(
          timestamp.longValue,
          getHistogram(methodName, className, metrics)
        )
      }.toSeq

    private def getHistogram(methodName: String, className: String, metrics: Seq[Map[String, Any]]) = MethodCallHistogram(
      methodName = methodName,
      className = className,
      count = metrics.getMatching[Double]("metricName", "duration.count")("metricValue").head.toLong,
      sum = metrics.getMatching("metricName", "duration.sum")("metricValue").head,
      min = metrics.getMatching("metricName", "duration.min")("metricValue").head,
      max = metrics.getMatching("metricName", "duration.max")("metricValue").head
    )
  }
}

class YarnLogAnalyzer(val agentReports: Seq[(String, Map[String, Any])]) extends DriverInformationProvider with ExecutorInformationProvider {

  val appId: String = agentReports.getAppId

  override val getDriverGcTime: Long = agentReports.getMetrics("driver", "CpuAndMemory").gcTime

  override val getExecutorGcTimes: Map[String, Long] = agentReports.getMetrics("executor", "CpuAndMemory")
    .groupBy(_("name").toString)
    .mapValues(_.gcTime)

  override val getDriverProcessCpuLoad: Seq[Measurement[Double]] = agentReports.getMeasurement("driver", "CpuAndMemory")("processCpuLoad")
  override val getDriverSystemCpuLoad: Seq[Measurement[Double]] = agentReports.getMeasurement("driver", "CpuAndMemory")("systemCpuLoad")

  override val getExecutorProcessCpuLoad: Map[String, Seq[Measurement[Double]]] = agentReports.getMetrics("executor", "CpuAndMemory")
    .groupBy(_("name").toString)
    .mapValues(_.getMeasurement("processCpuLoad"))

  override val getExecutorSystemCpuLoad: Map[String, Seq[Measurement[Double]]] = agentReports.getMetrics("executor", "CpuAndMemory")
    .groupBy(_("name").toString)
    .mapValues(_.getMeasurement("systemCpuLoad"))

  override val getDriverNameNodeLatencies: Seq[Measurement[MethodCallHistogram]] = agentReports.getMetrics("driver", "MethodDuration").nameNodeLatency

  override val getExecutorNameNodeLatencies: Map[String, Seq[Measurement[MethodCallHistogram]]] = agentReports.getMetrics("executor", "MethodDuration")
    .groupBy(_("processName").toString)
    .mapValues(_.nameNodeLatency)

}
