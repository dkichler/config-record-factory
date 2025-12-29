import sbt.ThisBuild

ThisBuild / organization            := "io.github.dkichler"
ThisBuild / scalaVersion            := "2.13.18"

ThisBuild / scmInfo                 := Option(
  ScmInfo(url("https://github.com/dkichler/config-record-factory"), "scm:git@github.com:dkichler/config-record-factory.git")
)
ThisBuild / developers              := List(
  Developer(
    id    = "dkichler",
    name  = "Dave Kichler",
    email = "@dkichler",
    url   = url("https://github.com/dkichler")
  )
)
ThisBuild / description             := "Extension for Lightbend Config to support creating Java Records from Config objects."
ThisBuild / licenses                := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / homepage                := Option(url("https://github.com/dkichler/config-record-factory"))
ThisBuild / pomIncludeRepository    := { _ => false }
ThisBuild / publishMavenStyle       := true

lazy val root = (project in file("."))
  .enablePlugins(DynVerPlugin)
  .enablePlugins(SbtOsgi)
  .enablePlugins(JacocoPlugin)
  .settings(osgiSettings)
  .settings(
    name                          := "config-record-factory",
    Compile / scalacOptions       ++= List("-unchecked", "-deprecation", "-feature"),
    Test / scalacOptions          ++= List("-unchecked", "-deprecation", "-feature"),
    Compile / javacOptions        ++= List("--release", "16"),
    Test / javacOptions           ++= List("--release", "16"),
    Compile / autoScalaLibrary    := false,
    Compile / crossPaths          := false,
    libraryDependencies           += "org.scala-lang" % "scala-library" % (ThisBuild / scalaVersion).value % Test,
    libraryDependencies           += "com.typesafe" % "config" % "1.4.5",
    libraryDependencies           += "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
    jacocoReportSettings          := JacocoReportSettings()
      .withFormats(JacocoReportFormats.HTML, JacocoReportFormats.CSV),

    OsgiKeys.exportPackage        := Seq("io.github.dkichler.config", "io.github.dkichler.config.impl"),
    Compile / packageBin / packageOptions  +=
      Package.ManifestAttributes("Automatic-Module-Name" -> "io.github.dkichler.config" ),

  )
