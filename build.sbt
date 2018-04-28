val publishSettings = Seq(
  publishTo := sonatypePublishTo.value,
  publishMavenStyle := true,
  description := "uuid util and z-base-32 encoding in Scala",
  pomExtra :=
    <url>https://github.com/ohze/szbase32</url>
    <licenses>
      <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/ohze/szbase32</url>
      <connection>scm:git@github.com:ohze/szbase32.git</connection>
    </scm>
    <developers>
      <developer>
        <id>giabao</id>
        <name>Gia Bảo</name>
        <email>giabao@sandinh.net</email>
        <organization>Sân Đình</organization>
        <organizationUrl>https://sandinh.com</organizationUrl>
      </developer>
    </developers>
)

val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.12.6",
  organization := "com.sandinh",
  scalacOptions := Seq("-encoding", "UTF-8", "-deprecation", "-target:jvm-1.8"),
  resolvers += Resolver.sonatypeRepo("releases")
)

lazy val szbase32 = (project in file("."))
  .settings(commonSettings ++ publishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.sandinh" %% "zbase32-commons-codec" % "1.0.0" % Test,
      "io.jvm.uuid" %% "scala-uuid" % "0.2.4" % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    ),
  )
