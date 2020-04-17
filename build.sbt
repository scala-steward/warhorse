import BuildHelper._

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(
  List(
    organization := "scash",
    homepage := Some(url("https://scala-cash.github.io/warhorse/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "sken",
        "sken",
        "sken77@pm.me",
        url("https://www.github.com/sken77")
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    scmInfo := Some(
      ScmInfo(url("https://github.com/scala-cash/warhorse/"), "scm:git:git@github.com:scala-cash/warhorse.git")
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
addCommandAlias("coverageGen", "coverage testkit/test core/coverageReport core/coverageAggregate core/coveralls")

val IntegrationTest = config("it") extend Test

lazy val warhorse =
  (project in file("."))
    .settings(
      stdSettings("warhorse")
    )
    .settings(buildInfoSettings("warhorse"))
    .enablePlugins(BuildInfoPlugin)
    .aggregate(core, testkit)

lazy val core = project
  .in(file("core"))
  .settings(stdSettings("core"))
  .settings(buildInfoSettings("core"))
  .settings(libraryDependencies ++= Deps.core)
  .enablePlugins(BuildInfoPlugin)

lazy val testkit = project
  .in(file("testkit"))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    stdSettings("testkit"),
    buildInfoSettings("testkit"),
    libraryDependencies ++= Deps.testkit,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .dependsOn(core, rpc)
  .enablePlugins(BuildInfoPlugin)

lazy val rpc = project
  .in(file("rpc"))
  .settings(
    stdSettings("rpc"),
    buildInfoSettings("rpc"),
    libraryDependencies ++= Deps.rpc
  )
  .dependsOn(core)
  .enablePlugins(BuildInfoPlugin)

lazy val docs = project
  .in(file("warhorse-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "warhorse-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(warhorse),
    target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
    cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(unidoc in Compile).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(unidoc in Compile).value
  )
  .dependsOn(warhorse)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
