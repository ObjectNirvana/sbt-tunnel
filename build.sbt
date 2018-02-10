sbtPlugin       := true

sbtVersion := "1.1.0"

organization  := "com.oni"

name := "sbt-tunnel"

version := "0.1.0-SNAPSHOT"

scalaVersion  := "2.12.3"

crossScalaVersions := Seq("2.11.11", "2.12.3")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
//  "com.lihaoyi" %% "utest" % "0.4.4" % "test",
//  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
)

//testFrameworks += new TestFramework("utest.runner.Framework")

unmanagedSourceDirectories in Test += baseDirectory(_ / "src" / "test" / "resources").value

// Sonatype
publishArtifact in Test := false

publishTo := version { (v: String) =>
  Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}.value

pomExtra := (
  <url>https://github.com/objectnirvana/sbt-tunnel</url>
    <licenses>
      <license>
        <name>MIT license</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git://github.com/objectnirvana/sbt-tunnel.git</url>
      <connection>scm:git://github.com/objectnirvana/sbt-tunnel.git</connection>
    </scm>
    <developers>
      <developer>
        <id>michaeldmccray</id>
        <name>Michael McCray</name>
        <url>https://github.com/MichaelDMcCray</url>
      </developer>
    </developers>
  )
