// sbt-git

enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

git.useGitDescribe := true

// sbt-release

import sbtrelease._
import ReleaseKeys._
import Utilities._
import ReleaseStateTransformations._

releaseVersionBump := Version.Bump.Bugfix

// releaseNextVersion := { currentVersion =>
//   Version(currentVersion).map(_.bumpNano.withoutQualifier.string).getOrElse(versionFormatError)
// }


lazy val confirmVersion: ReleaseStep = { st: State =>
  val extracted = Project.extract(st)
  val currentVersion = extracted.get(version)
  val suggestedReleaseVersion = extracted.get(releaseNextVersion)(currentVersion)

  println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  println(currentVersion)
  println(extracted.get(git.gitDescribedVersion))
  println(
    git.describeVersion(extracted.get(git.gitDescribedVersion), "")
  )
  println(
    git.makeUncommittedSignifierSuffix(
      extracted.get(git.gitUncommittedChanges),
      extracted.get(git.uncommittedSignifier)
    )
  )
  
  val releaseVersion = readVersion(suggestedReleaseVersion, "Release version [%s] : ", st.get(useDefaults).getOrElse(false), st.get(commandLineReleaseVersion).flatten)
  
  st.log.info("Setting version to '%s'." format releaseVersion)
  
  val useGlobal = st.extract.get(releaseUseGlobalVersion)
  reapply(Seq(
    if (useGlobal) version in ThisBuild := releaseVersion
    else version := releaseVersion
  ), st)
}

releaseProcess := Seq[ReleaseStep](
//   checkSnapshotDependencies,
//   initialVcsChecks,
//   inquireVersions,
  confirmVersion,
  runClean,
  runTest,
//   setReleaseVersion,
//   commitReleaseVersion,
  tagRelease,
//   publishArtifacts,
//   setNextVersion,
//   commitNextVersion,
  pushChanges
)