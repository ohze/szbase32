import mill._, scalalib._, publish._

trait PublishBased extends PublishModule {
  def publishVersion = "1.0.0"
  def pomSettings = PomSettings(
    description = "uuid util & z-base-32 encoding in Scala",
    organization = "com.sandinh",
    url = "https://github.com/ohze/szbase32",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("ohze", "szbase32"),
    developers = Seq(
      Developer("ohze", "Bui Viet Thanh", "https://github.com/ohze"),
      Developer("haakonn", "Haakon Nilsen", "https://github.com/haakonn")
    )
  )
}

trait ScalaBased extends ScalaModule {
  def scalaVersion = "2.12.5"
  override def scalacOptions = T {
    Seq("-encoding", "UTF-8", "-deprecation", "-target:jvm-1.8")
  }
}

object szbase32 extends PublishBased with ScalaBased {
  object test extends Tests {
    override def ivyDeps = Agg(
      ivy"io.jvm.uuid::scala-uuid:0.2.4",
      ivy"org.scalatest::scalatest:3.0.5"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}
