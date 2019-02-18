import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile

addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17")

name := "global"

version := "0.1"

scalaVersion := "2.12.7"

lazy val global =
  (project in file(".") dependsOn (modules.map(_ % "test->test; compile->compile"): _*) aggregate (modules: _*))
    .enablePlugins(DockerPlugin, JavaAppPackaging, AshScriptPlugin)

lazy val modules =
  ProjectRef(base = file("../../modules/upload/server-scala"), id = "upload") ::
    ProjectRef(base = file("../../modules/user/server-scala"), id = "user") ::
    ProjectRef(base = file("../../modules/counter/server-scala"), id = "counter") ::
    ProjectRef(base = file("../../modules/contact/server-scala"), id = "contact") ::
    ProjectRef(base = file("../../modules/pagination/server-scala"), id = "pagination") ::
    ProjectRef(base = file("../../modules/authentication/server-scala"), id = "authentication") ::
    ProjectRef(base = file("../../modules/post/server-scala"), id = "post") ::
    ProjectRef(base = file("../../modules/chat/server-scala"), id = "chat") ::
    Nil

resourceGenerators in Compile ++= Seq(
  ResourceProcessor.concatDotEnvsTask.taskValue,
  ResourceProcessor.concatServerConfigsTask.taskValue
)

packageName in Docker := "scala_server"
dockerBaseImage := "openjdk:jre-alpine"
dockerExposedPorts := Seq(8080)
defaultLinuxInstallLocation in Docker := "/usr/local"
dockerExposedVolumes := Seq("/usr/local", "/usr/local/target")

mainClass in Compile := Some("Main")

scalafmtOnCompile := true
scalafmtConfig := Some(file("../../.scalafmt.conf"))
