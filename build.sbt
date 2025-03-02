ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2023)

ThisBuild / crossScalaVersions := Seq("3.3.1")

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / tlJdkRelease := Some(8)

ThisBuild / githubWorkflowBuildPreamble ++= Seq(
  WorkflowStep.Use(
    UseRef.Public("actions", "setup-node", "v4"),
    name = Some("Setup NodeJS v20 LTS"),
    params = Map("node-version" -> "20", "cache" -> "npm"),
    cond = Some("matrix.project == 'rootJS'"),
  ),
  WorkflowStep.Run(
    List("npm install"),
    cond = Some("matrix.project == 'rootJS'"),
  ),
)

ThisBuild / githubWorkflowBuildPreamble ++= nativeBrewInstallWorkflowSteps.value
ThisBuild / nativeBrewInstallCond := Some("matrix.project == 'rootNative'")

ThisBuild / Test / testOptions += Tests.Argument("+l")

lazy val root = tlCrossRootProject.aggregate(
  core,
)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("core"))
  .settings(
    name := "porcupine",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.9.0",
      "org.typelevel" %%% "cats-effect" % "3.5.4",
      "org.typelevel" %%% "cats-core" % "2.10.0",
      "co.fs2" %%% "fs2-core" % "3.11.0",
      "org.scodec" %%% "scodec-bits" % "1.1.38",
    ),
    Test / test := (Test / run).toTask("").value,
    Test / mainClass := Some("porcupine.PorcupineTest"),
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.xerial" % "sqlite-jdbc" % "3.46.1.3",
    ),
    fork := true,
  )
  .jsSettings(
    Test / scalaJSUseMainModuleInitializer := true,
    Test / scalaJSUseTestModuleInitializer := false,
    Test / scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    jsEnv := {
      import org.scalajs.jsenv.nodejs.NodeJSEnv
      new NodeJSEnv(NodeJSEnv.Config().withArgs(List("--enable-source-maps")))
    },
  )
  .nativeConfigure(_.enablePlugins(ScalaNativeBrewedConfigPlugin))
  .nativeSettings(
    nativeBrewFormulas += "sqlite",
    nativeConfig ~= { c => c.withLinkingOptions(c.linkingOptions :+ "-lsqlite3") },
  )
