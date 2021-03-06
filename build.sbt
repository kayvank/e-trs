import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.SettingsHelper._
import TodoListPlugin._

name := "e-trs"

organization := "vevo"

scalaVersion := "2.11.8"

compileWithTodolistSettings

testWithTodolistSettings

val gocdPipelineCounter = settingKey[String]("gocdPipelineCounter")

gocdPipelineCounter := sys.props.getOrElse("GO_PIPELINE_COUNTER", default = "000")

lazy val root = (project in file("."))
  .configs(IntegrationTest).settings(Defaults.itSettings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    JavaAppPackaging,
    DockerPlugin,
    UniversalPlugin).settings(
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      buildInfoBuildNumber),
    buildInfoPackage := "info",
    buildInfoOptions ++= Seq(BuildInfoOption.BuildTime, BuildInfoOption.ToJson)
  )

scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-encoding", "UTF-8",
  "-feature",
  "-Xlog-reflective-calls",
  "-Ywarn-unused",
  //"-Ylog-classpath", // show me the classpath
  "-Ywarn-value-discard",
  "-Xlint",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Xfuture",
  "-language:postfixOps",
  "-language:implicitConversions"
)

resolvers ++= Seq(
   Resolver.sonatypeRepo("snapshots"),
  "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= {
  object V {
    val specs2 = "3.7"
    val scalacheck = "1.13.2"
    val http4s = "0.15.0a"
    val circe = "0.6.1"
    val kcl = "1.7.4"
  }
  Seq(
    "com.amazonaws" % "amazon-kinesis-client" % V.kcl,
    "org.http4s" %% "http4s-blaze-server" % V.http4s,
    "org.http4s" %% "http4s-blaze-client" % V.http4s,
    "org.http4s" %% "http4s-dsl" % V.http4s,
    "org.http4s" %% "http4s-circe" % V.http4s,
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-parser" % V.circe,
    "io.circe" %% "circe-optics" % V.circe,
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-parser" % V.circe,
    "io.circe" %% "circe-optics" % V.circe,
    "ch.qos.logback" % "logback-classic" % "1+",
    "com.typesafe.scala-logging" %% "scala-logging" % "3+",
    "com.typesafe" % "config" % "1.2.1",
    "org.specs2" %% "specs2-core" % V.specs2 % "test",
    "org.specs2" %% "specs2-scalacheck" % V.specs2 % "test",
    "org.scalaz" %% "scalaz-scalacheck-binding" % "7.2.0+" % "test",
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3" % "test",
    "com.lihaoyi" % "ammonite" % "0.8.0" % "test" cross CrossVersion.full
)}

parallelExecution in Test := false

buildInfoKeys += buildInfoBuildNumber

buildInfoOptions += BuildInfoOption.BuildTime

publishMavenStyle := true

dockerExposedPorts := Seq(9000, 9443)

maintainer in Docker := "admin@vevo.com"

version in Docker := version.value +
  "-b" + sys.props.getOrElse("build_number", default = "dev")

dockerRepository := Some("vevo")

dockerCommands ++= Seq()

dockerUpdateLatest := true

Seq(bintrayResolverSettings: _*)

deploymentSettings

publish <<= publish.dependsOn(publish in config("universal"))

coverageEnabled.in(Test, test) := true

javaOptions += "-Xmx512M"

aspectjSettings

fork in run := true // required for kamon

fork in test := false // required for kamon

javaOptions <++= AspectjKeys.weaverOptions in Aspectj

javaOptions in Universal ++= Seq(
  "-J-Xmx512M",
  "-J-Xms256M"
)
