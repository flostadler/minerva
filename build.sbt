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
    history
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
  .dependsOn(history)
  .settings(
    name := "minerva-cmd",
    settings,
    assemblySettings,
    mainClass in assembly := Some("de.flostadler.minerva.cmd.Command"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false),
    libraryDependencies ++= cmdDependencies
  )

lazy val history = project
  .in(file("history"))
  .dependsOn(core)
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "minerva-history",
    settings,
    libraryDependencies ++= historyDependencies
  )


lazy val coreDependencies = Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
)

lazy val cmdDependencies = Seq(
  "org.rogach" %% "scallop" % "3.3.2",
  "org.jline" % "jline-terminal" % "3.13.2",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

lazy val historyDependencies = Seq(
  "com.jsuereth" %% "scala-arm" % "2.0",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

lazy val jacksonVersion = "2.9.9"
lazy val json4sVersion = "3.6.7"
lazy val  sparkVersion = "2.3.0"

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
    case _ => MergeStrategy.first
  }
)
