package tunnel.plugin

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.FileIO
import sbt.io.Paths
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.Path
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.ByteString

case class SshTunnelArgs(
  host: String,
  serverPort: Int,
  localPort: Int,
  localhost: String,
  key: String,
  sshport: Int,
  sshcmdx: String = "") {
  def sshcmd = s"""ssh -v -i ${key} -p ${sshport} root@${host} -L $serverPort:${localhost}:$localPort -N"""
}

case class SshTunnelConfig(
    ports: List[SshTunnelArgs])

object TunnelConfig {
  def export(name: String)(implicit system: ActorSystem, mat: Materializer): Unit = {
    val sta = SshTunnelArgs(
        host = "host",
        serverPort = 29002,
        localPort = 29002,
        localhost = "127.0.0.1",
        key= "key",
        sshport = 2222)
    val stc = SshTunnelConfig(List(sta))
    val json = stc.asJson
    //val json = stc.asJson.noSpaces

    println(json)

    val dest = FileIO.toPath(Paths.get(name))
    dest.runWith(Source.single(ByteString(json.toString)))
    ()
  }
//  def import(name: String): SshTunnelConfig = {
//    
//  }
}
