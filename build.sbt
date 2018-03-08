sbtPlugin       := true

sbtVersion := "1.1.1"

organization  := "com.objectnirvana.sbt"

name := "sbt-tunnel"

useGpg := true

pgpReadOnly := true

// gpgCommand := "/usr/local/bin/gpg"

// pgpSecretRing := file("/path/to/my/secring.gpg")

scalaVersion  := "2.12.4"

// POM settings for Sonatype
homepage := Some(url("https://github.com/ObjectNirvana/sbt-tunnel"))
scmInfo := Some(ScmInfo(url("https://github.com/ObjectNirvana/sbt-tunnel"),
                            "git@github.com:ObjectNirvana/sbt-tunnel.git"))
developers := List(Developer("username",
                             "Michael McCray",
                             "mike@objectnirvana.com",
                             url("https://github.com/ObjectNirvana")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true


crossScalaVersions := Seq("2.11.11", "2.12.4")

val circeVersion = "0.9.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.0.2" % "test")

val latestJson4sVersion = "3.5.3"
val json4sNative = "org.json4s" %% "json4s-native" % latestJson4sVersion

libraryDependencies += json4sNative

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
//  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
  "ch.qos.logback" % "logback-classic" % "1.1.3",  
  "org.slf4j" % "slf4j-api" % "1.7.25"
  //"com.typesafe.scala-logging" %% "scala-logging-slf4j" % "3.5.0"
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

val AkkaVersion = "2.5.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-typed" % AkkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "0.16"
)

//testFrameworks += new TestFramework("utest.runner.Framework")

//unmanagedSourceDirectories in Test += baseDirectory(_ / "src" / "test" / "resources").value

// Sonatype
publishArtifact in Test := false

//publishTo := version { (v: String) =>
//  Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
//}.value

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

pomExtra := (
  <url>https://github.com/ObjectNirvana/sbt-tunnel</url>
    <licenses>
      <license>
        <name>MIT license</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git://github.com/ObjectNirvana/sbt-tunnel.git</url>
      <connection>scm:git://github.com/ObjectNirvana/sbt-tunnel.git</connection>
    </scm>
    <developers>
      <developer>
        <id>michaeldmccray</id>
        <name>Michael McCray</name>
        <url>https://github.com/MichaelDMcCray</url>
      </developer>
    </developers>
  )
