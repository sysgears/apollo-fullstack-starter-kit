name := "global"

version := "0.1"

scalaVersion := "2.12.7"

lazy val global = project in file(".") dependsOn(upload, user, pagination) aggregate(user, upload, pagination)

lazy val upload = ProjectRef(base = file("../../modules/upload/server-scala"), id = "upload")

lazy val user = ProjectRef(base = file("../../modules/user/server-scala"), id = "user")

lazy val pagination = ProjectRef(base = file("../../modules/pagination/server-scala"), id = "pagination")