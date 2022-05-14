ThisBuild / organization := "com.ivan.project"
ThisBuild / version := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
ThisBuild / scalaVersion := "2.13.8"

// Workaround for scala-java8-compat issue affecting Lagom dev-mode
// https://github.com/lagom/lagom/issues/3344
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always

val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "10.0.2"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `employ` = (project in file("."))
  .aggregate(`employ-api`, `employ-impl`, `employ-stream-api`, `employ-stream-impl`)

lazy val `employ-api` = (project in file("employ-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
        playJsonDerivedCodecs
    )
  )

lazy val `employ-impl` = (project in file("employ-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`employ-api`)

lazy val `employ-stream-api` = (project in file("employ-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
    )
  )

lazy val `employ-stream-impl` = (project in file("employ-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`employ-stream-api`, `employ-api`)
