// sbt-git

import git._

enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

useGitDescribe := true
uncommittedSignifier := None

// sbt-release

import sbtrelease._
import ReleaseKeys._
import Utilities._
import ReleaseStateTransformations._

releaseNextVersion := { currentVersion =>
  Version(currentVersion).map(_.bumpBugfix.withoutQualifier.string).getOrElse(versionFormatError)
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
  checkUnstagedAndUntracked,
  confirmVersion,
  runClean,
  runTest,
  tagRelease,
//   publishArtifacts,
  pushChanges
)