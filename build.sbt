name := "minerva"
version := "0.0.1"
organization in ThisBuild := "de.flostadler.minerva"
scalaVersion in ThisBuild := "2.11.12"

lazy val global = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    core,
    cmd,
    history,
    profiler
  )

lazy val core = project
  .in(file("core"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "minerva-core",
    settings,
    libraryDependencies ++= coreDependencies
  )

lazy val cmd = project
  .in(file("cmd"))
  .settings(
    name := "minerva-cmd",
    settings,
    assemblySettings
  )

lazy val history = project
  .in(file("history"))
  .dependsOn(core)
  .settings(
    name := "minerva-history",
    settings,
    assemblySettings,
    libraryDependencies ++= sparkDependencies map { _.excludeAll(exclusionRules: _*) }
  )

lazy val profiler = project
  .in(file("profiler"))
  .settings(
    name := "minerva-profiler",
    settings,
    assemblySettings,
    libraryDependencies ++= profilerDependencies
  )

lazy val profilerDependencies = Seq(
  "io.vavr" % "vavr" % "0.10.2" withSources()
)

lazy val coreDependencies = Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
)

// Dependency Version
lazy val commonsCodecVersion = "1.10"
lazy val commonsIoVersion = "2.4"
lazy val gsonVersion = "2.2.4"
lazy val guavaVersion = "18.0"          // Hadoop by default uses Guava 11.0, might raise NoSuchMethodException
lazy val jacksonVersion = "2.9.9"
lazy val jerseyVersion = "2.24"
lazy val jsoupVersion = "1.7.3"
lazy val mysqlConnectorVersion = "5.1.36"
lazy val oozieClientVersion = "4.2.0"
lazy val tonyVersion = "0.3.16"
lazy val vavrVersion = "0.10.2"
lazy val json4sVersion = "3.6.7"
lazy val sparklensVersion = "0.3.1-s_2.11"

lazy val  hadoopVersion = "2.7.3"
lazy val  sparkVersion = "2.3.2"

lazy val sparkExclusion = "org.apache.spark" % "spark-core_2.11" % sparkVersion excludeAll(
  ExclusionRule(organization = "com.typesafe.akka"),
  ExclusionRule(organization = "org.apache.avro"),
  ExclusionRule(organization = "org.apache.hadoop"),
  ExclusionRule(organization = "net.razorvine")
)

// Dependency coordinates
lazy val sparkDependencies = Seq(
  "qubole" % "sparklens" % sparklensVersion withSources(),
  "org.json4s" %% "json4s-native" % json4sVersion,
  "io.vavr" % "vavr" % vavrVersion,
  "com.google.code.gson" % "gson" % gsonVersion,
  "com.google.guava" % "guava" % guavaVersion,
  "com.jsuereth" %% "scala-arm" % "1.4",
  "commons-codec" % "commons-codec" % commonsCodecVersion,
  "commons-io" % "commons-io" % commonsIoVersion,
  "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
  "mysql" % "mysql-connector-java" % mysqlConnectorVersion,
  "org.apache.hadoop" % "hadoop-auth" % hadoopVersion % "compileonly",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % hadoopVersion % "compileonly",
  "org.apache.hadoop" % "hadoop-mapreduce-client-common" % hadoopVersion % "compileonly",
  "org.apache.hadoop" % "hadoop-mapreduce-client-common" % hadoopVersion % Test,
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % hadoopVersion % Test,
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % "compileonly",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test,
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % "compileonly",
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test,
  "org.jsoup" % "jsoup" % jsoupVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.oozie" % "oozie-client" % oozieClientVersion excludeAll(
    ExclusionRule(organization = "org.apache.hadoop")
    ),
  "org.glassfish.jersey.core" % "jersey-client" % jerseyVersion,
  "org.glassfish.jersey.core" % "jersey-common" % jerseyVersion,
  "org.glassfish.jersey.media" % "jersey-media-json-jackson" % jerseyVersion % Test,
  "org.glassfish.jersey.test-framework" % "jersey-test-framework-core" % jerseyVersion % Test,
  "org.glassfish.jersey.test-framework.providers" % "jersey-test-framework-provider-grizzly2" % jerseyVersion % Test,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
  "io.dropwizard.metrics" % "metrics-core" % "3.1.2",
  "io.dropwizard.metrics" % "metrics-healthchecks" % "3.1.2",
  "org.mockito" % "mockito-core" % "1.10.19" exclude ("org.hamcrest", "hamcrest-core"),
  "org.jmockit" % "jmockit" % "1.23" % Test,
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "org.apache.httpcomponents" % "httpcore" % "4.4.4",
  "org.scalatest" %% "scalatest" % "3.0.0" % Test,
  "com.h2database" % "h2" % "1.4.196" % Test,
  "com.linkedin.tony" % "tony-core" % tonyVersion excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "org.apache.hadoop")
  )
) :+ sparkExclusion

lazy val exclusionRules = Seq(
  ExclusionRule(organization = "com.sun.jersey", name = "jersey-core"),
  ExclusionRule(organization = "com.sun.jersey", name = "jersey-server")
)

lazy val settings = commonSettings

lazy val CompileOnly = config("compileonly").hide

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    "SparkPackages" at "https://dl.bintray.com/spark-packages/maven/"
  ),
  ivyConfigurations += CompileOnly
)

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)
