package tunnel.plugin

//import tools.nsc.Global
//import scala.collection.SortedSet
//import io.Source

//import sbt._
//import Keys._
//import org.mozilla.javascript.{ScriptableObject, ContextFactory, Context, Function => JsFunction}
//import org.mozilla.javascript.tools.shell.{Global, Main}
//import java.io.{FileReader, InputStreamReader}
//import sbt.TaskKey
//import sbt.Plugin
//import sbt.Keys._
//import sbt._
import sbt._
import Keys._
import sbt.complete.Parsers._
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object SbtTunnelPlugin extends AutoPlugin {

  /**
   * Sets up the autoimports of setting keys.
   */
  object autoImport {

    val tun1 = inputKey[Unit]("Says hello!")
    // val ports = inputKey[Unit]("the ports")

    /**
     * Defines "rssList" as the setting key that we want the user to fill out.
     */
    val rssList = settingKey[Seq[String]]("The list of RSS urls to update.")
  }

  import autoImport._

  override def globalSettings: Seq[Setting[_]] = super.globalSettings ++ Seq(
    Keys.commands += rssCommand)

  var tun1a: Future[Int] = null

  def openTunnel = {
    val host = "54.201.39.76"
    val key = "~/.ssh/gvsupport_key"
    // val sshcmd = s"""ssh -v -i ${key} -p $sshport root@${host} -L $serverPort:${localhost}:$localPort -N"""
    val sta = SshTunnelArgs(
      host = host,
      serverPort = 29002,
      localPort = 29002,
      localhost = "127.0.0.1",
      key = key,
      sshport = 2222)
    import sys.process._
    val logger = ProcessLogger(
      (o: String) => println("out " + o),
      (e: String) => println("err " + e))
    import scala.concurrent.ExecutionContext
    implicit val ec = ExecutionContext.global
    val result = Future {
      sta.sshcmd ! logger
    }
    tun1a = result
    println(s"result = $result")
  }

  var cfg: SshTunnelConfig = null

  implicit val system: ActorSystem = ActorSystem("tunnel", ConfigFactory.parseString("""
akka {
  loglevel = "INFO"

  version = "2.5.6"

  //loggers = ["akka.event.slf4j.Slf4jLogger"]

  //logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
  actor {
    provider = "local"
    guardian-supervisor-strategy = "akka.actor.DefaultSupervisorStrategy"
    creation-timeout = 20s

    debug {
      # enable function of LoggingReceive, which is to log any received message at
      # DEBUG level
      receive = on
      autoreceive = on
      lifecycle = on
    }
  }
}""")) // (greeter, "hello")
  private implicit val mat = ActorMaterializer()

  def opTunnel(args: Seq[String]) = {
    args(0) match {
      case "export" =>
        TunnelConfig.export("target/tunnel-config.json")
      case "import" =>
        // cfg = TunnelConfig.import("src/test/resources/tunnel-config.json")
      case "open" =>
        openTunnel
      case x =>
        println(s"error: unknown ${x}")
    }
  }

  override lazy val projectSettings = Seq(
    tun1 := {
      val args = spaceDelimited("").parsed
      args foreach { arg =>
        println(s"arg:$arg")
      }
      opTunnel(args)
    }
  //    ports := {
  //      println(s"ports here")
  //    }
  )

  /** Allows the RSS command to take string arguments. */
  private val args = (Space ~> StringBasic).*

  /** The RSS command, mapped into sbt as "rss [args]" */
  private lazy val rssCommand = Command("rss")(_ => args)(doRssCommand)

  def doRssCommand(state: State, args: Seq[String]): State = {
    // do stuff
    println(s"doing rss command: $state, args: ${args.mkString(",")}")
    state
  }

  // Doing Project.extract(state) and then importing it gives us currentRef.
  // Using currentRef allows us to get at the values of SettingKey.
  // http://www.scala-sbt.org/release/docs/Build-State.html#Project-related+data
  //val extracted = Project.extract(state)
  //import extracted._

  //  lazy val jasmineTestDir = SettingKey[Seq[File]]("jasmineTestDir", "Path to directory containing the /specs and /mocks directories")
  //  lazy val appJsDir = SettingKey[Seq[File]]("appJsDir", "the root directory where the application js files live")
  //  lazy val appJsLibDir = SettingKey[Seq[File]]("appJsLibDir", "the root directory where the application's js library files live")
  //  lazy val jasmineConfFile = SettingKey[Seq[File]]("jasmineConfFile", "the js file that loads your js context and configures jasmine")
  //  lazy val jasmineRequireJsFile = SettingKey[Seq[File]]("jasmineRequireJsFile", "the require.js file used by the application")
  //  lazy val jasmineRequireConfFile = SettingKey[Seq[File]]("jasmineRequireConfFile", "the js file that configures require to find your dependencies")
  lazy val tunnel = TaskKey[Unit]("tunnel", "Open tunnel")

  //  lazy val jasmineOutputDir = SettingKey[File]("jasmineOutputDir", "directory to output jasmine files to.")
  //  lazy val jasmineGenRunner = TaskKey[Unit]("jasmine-gen-runner", "Generates a jasmine test runner html page.")

  println("this here")

  //  def tunnelTask = (jasmineTestDir, appJsDir, appJsLibDir, jasmineConfFile, jasmineOutputDir, streams) map { (testJsRoots, appJsRoots, appJsLibRoots, confs, outDir, s) =>
  //
  //    s.log.info("running jasmine...")
  //  }
}

//class RuntimePlugin(global: Global) extends TestPlugin(global)

/*
class TestPlugin(val global: Global)
                 // cycleReporter: Seq[(Value, SortedSet[Int])] => Unit = _ => ())
  extends tools.nsc.plugins.Plugin {

  val name = "sbt-tunnel"

  var force = false
  override def processOptions(options: List[String], error: String => Unit): Unit = {
    if (options.contains("force")) {
      force = true
    }
  }
  val description = "manages tunnels to cloud based resources"


  val components = List[tools.nsc.plugins.PluginComponent](
    new PluginPhase(this.global, /*cycleReporter,*/ force)
  )
}
*/

