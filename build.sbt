name := "sbt-test"

scalaVersion := "2.11.6"

organization := "org.uqbar"

organizationName := "Uqbar Foundation"

organizationHomepage := Some(url("http://www.uqbar-project.org"))

licenses += "LGPLv3" -> url("https://www.gnu.org/licenses/lgpl.html")

homepage := Some(url("http://www.uqbar.org"))

// scalacOptions ++= Seq("-unchecked", "-deprecation")

// crossScalaVersions := Seq(scalaVersion.value)

//////////////////////////////////////////////////////////////////////
// sbt-git

import git._
import com.typesafe.sbt.SbtGit._

enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

val snapshotPattern = ".*-.*"

useGitDescribe := true
uncommittedSignifier := None
isSnapshot := snapshotPattern matches version.value

//////////////////////////////////////////////////////////////////////
// sbt-sonatype

// publishTo <<= version { v: String =>
//   val nexus = "https://oss.sonatype.org/"
//   if (v.trim.endsWith("SNAPSHOT"))
//     Some("snapshots" at nexus + "content/repositories/snapshots")
//   else
//     Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }


credentials += Credentials("Sonatype Nexus Repository Manager",
                           "oss.sonatype.org",
                           "nscarcella",
                           "keyberry")

pgpPassphrase := Some("keyberry".toCharArray)

pomExtra := (
  <scm>
    <url>https://github.com/uqbar-project/{name.value}</url>
    <connection>scm:git@github.com:uqbar-project/{name.value}.git</connection>
  </scm>
  <developers>
    <developer>
      <id>uqbar</id>
      <name>Uqbar</name>
      <url>http://www.uqbar.org</url>
    </developer>
  </developers>
)

//////////////////////////////////////////////////////////////////////
// sbt-release

import sbtrelease._
import ReleaseKeys._
import Utilities._
import ReleaseStateTransformations._

releaseNextVersion := { currentVersion =>
  Version(currentVersion).map(_.bumpBugfix.withoutQualifier.string).getOrElse(versionFormatError)
}

releaseTagComment := {
  val lastTag = Process("git describe --tags --abbrev=0").lines.headOption
  val changeWindow = lastTag.fold("HEAD"){_ + "..HEAD"}
  val logs = Process(s"git log $changeWindow").lines.map(_.trim).filter(_.startsWith('*'))
  
  s"""${releaseTagName.value}\n\n${logs.mkString("\n")}"""
}

lazy val confirmVersion: ReleaseStep = { st: State =>
  val extracted = Project.extract(st)
  val currentVersion = extracted.get(version)
  val versionPattern = """([^-]*)-?.*""".r
  val versionPattern(baseVersion) = currentVersion
  val suggestedReleaseVersion = extracted.get(releaseNextVersion)(baseVersion)
  
  println(s"Current version: $currentVersion")
  val releaseVersion = readVersion(
    suggestedReleaseVersion,
    "Release version [%s] : ",
    st.get(useDefaults).getOrElse(false),
    st.get(commandLineReleaseVersion).flatten
  )

  reapply(Seq(releaseTagName := s"v$releaseVersion"), st)
}

lazy val checkUnstagedAndUntracked = { st: State =>
  val extracted = Project.extract( st )
  val vcs = st.extract.get(releaseVcs).getOrElse(sys.error("Aborting release: Working directory is not a repository of a recognized VCS."))
  val hasUntrackedFiles = vcs.hasUntrackedFiles
  val hasModifiedFiles = vcs.hasModifiedFiles
  if ( hasModifiedFiles ) sys.error( "Aborting release: Unstaged modified files" )
  if ( hasUntrackedFiles && !extracted.get( releaseIgnoreUntrackedFiles ) ) sys.error( "Aborting release: Untracked files. Remove them or specify 'releaseIgnoreUntrackedFiles := true' in settings" )
  st
}

releaseProcess := Seq[ReleaseStep](
//   checkUnstagedAndUntracked,
  ReleaseStep(action = Command.process("git pull", _)),
  confirmVersion,
  runClean,
  runTest,
  tagRelease,
  ReleaseStep(action = Command.process("git push --follow-tags", _))
//   ReleaseStep(action = Command.process("publishSigned", _)),
//   ReleaseStep(action = Command.process("sonatypeReleaseAll", _))
)