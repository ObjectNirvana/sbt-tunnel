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
import sys.process._
import scala.concurrent.ExecutionContext
import java.net.ServerSocket

object SbtTunnelPlugin extends AutoPlugin {

  /**
   * Sets up the autoimports of setting keys.
   */
  object autoImport {
    val tunnel = inputKey[Unit]("Says hello!")
  }

  import autoImport._

  /** Allows the RSS command to take string arguments. */
  private val args = (Space ~> StringBasic).*

  /** The tunnel command, mapped into sbt as "tunnel [args]" */
  private lazy val tunnelCommand = Command("tunnel")(_ => args)(doTunnelCommand)

  // lazy val tunnel1 = TaskKey[Unit]("tunnel", "Open tunnel")

  def doTunnelCommand(state: State, args: Seq[String]): State = {
    println(s"tunnel command: $state, args: ${args.mkString(",")}")

    args(0) match {
      case "export" =>
        TunnelConfig.export("target/tunnel-config.json")
      case "import" =>
        println(s"reading from ${args(1)}")
        stcfg = TunnelConfig.importConfig(args(1)) // "src/test/resources/tunnel-config.json")
      case "open" =>
        openTunnel(args(1))
      case "stop" =>
        closeTunnel(args(1))
      case "test" =>
        testTunnel(args(1))
      case x =>
        println(s"error: unknown ${x}")
    }
    state
  }

  override def globalSettings: Seq[Setting[_]] = super.globalSettings ++ Seq(
    Keys.commands += tunnelCommand)

  case class TunnelData(
      config: SshTunnelArgs,
      pid: Option[Int] = None,
      thread: Thread)

  var tunnels: Map[String, TunnelData] = Map()

  def closeTunnel(name: String): Unit = {
    println("close tunnel")
    val pid = tunnels(name).pid
    val r = s"kill $pid" !

    val current = tunnelSync.synchronized {
      val c = tunnels(name)
      tunnels = tunnels - name
      c
    }
    current.thread.join()

    println(s"after kill tunnel r=$r")
  }

  def testTunnel(name: String): Unit = {
    println("test tunnel")
    val sta = stcfg.ports.filter(_.tunnelName == name).head
    var s: ServerSocket = null
    try {
      s = new ServerSocket(sta.localPort)
      println("port open")
    } catch {
      case t: Throwable =>
        println("port taken")
        t.printStackTrace()
    } finally {
      s.close()
    }
  }

  def openTunnel(name: String) = {
//    val host = "54.201.39.76"
//    val key = "~/.ssh/gvsupport_key"
//    // val sshcmd = s"""ssh -v -i ${key} -p $sshport root@${host} -L $serverPort:${localhost}:$localPort -N"""
//    val sta = SshTunnelArgs(
//        tunnelName = "tn",
//      host = host,
//      serverPort = 29002,
//      localPort = 29002,
//      localhost = "127.0.0.1",
//      key = key,
//      sshport = 2222)
    val sta = stcfg.ports.filter(_.tunnelName == name).head
    val logger = ProcessLogger(
      (o: String) => println("out " + o),
      (e: String) => println("err " + e))
    implicit val ec = ExecutionContext.global
    val t = new Thread(() => {
      val result = sta.sshcmd ! logger
      updatePid(sta.tunnelName, result)
      println(s"setting pit = $result for $name")
    })
    tunnels = tunnels.updated(sta.tunnelName, TunnelData(thread = t, config = sta))
    t.start
  }

  private val tunnelSync = new Object

  def updatePid(name: String, pid: Int) = {
    tunnelSync.synchronized {
      val orig = tunnels(name)
      tunnels = tunnels.updated(name, orig.copy(pid = Some(pid)))
    }
  }

  var stcfg: SshTunnelConfig = null

//  override lazy val projectSettings = Seq(
//    tun1 := {
//      val args = spaceDelimited("").parsed
//      args foreach { arg =>
//        println(s"arg:$arg")
//      }
//    }
//  )

}


