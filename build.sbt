//////////////////////////////////////////////////////////////////////
// Build

name := "sbt-test"

description := "some test project"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq()

//////////////////////////////////////////////////////////////////////
// sbt-git

import git._
import com.typesafe.sbt.SbtGit._
import com.typesafe.sbt.GitBranchPrompt


import sbtrelease._
import ReleaseKeys._
import Utilities._
import ReleaseStateTransformations._

//--------------------------------------------------------------------
// release notes

releaseTagComment := {
  val lastTag = Process("git describe --tags --abbrev=0").lines.headOption
  val changeWindow = lastTag.fold("HEAD"){_ + "..HEAD"}
  val logs = Process(s"git log --pretty=%B $changeWindow").lines.map(_.trim).filter(_.startsWith("*"))
  
  s"""${releaseTagName.value}\n\n${logs.mkString("\n")}"""
}

//--------------------------------------------------------------------
