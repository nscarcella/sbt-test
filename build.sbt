//////////////////////////////////////////////////////////////////////
// Build

name := "sbt-test"

description := "some test project"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq()

//--------------------------------------------------------------------
// release notes

//releaseTagComment := {
//  val lastTag = Process("git describe --tags --abbrev=0").lines.headOption
//  val changeWindow = lastTag.fold("HEAD"){_ + "..HEAD"}
//  val logs = Process(s"git log --pretty=%B $changeWindow").lines.map(_.trim).filter(_.startsWith("*"))
//  
//  s"""${releaseTagName.value}\n\n${logs.mkString("\n")}"""
//}