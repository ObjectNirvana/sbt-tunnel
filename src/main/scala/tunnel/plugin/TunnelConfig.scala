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
import sbt.io.IO
import java.io.File
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}

/*
scala> implicit val formats = Serialization.formats(NoTypeHints)

scala> val ser = write(Child("Mary", 5, None))

scala> read[Child](ser)
res1: Child = Child(Mary,5,None)
If you're using jackson instead of the native one:

scala> import org.json4s._
scala> import org.json4s.jackson.Serialization
scala> import org.json4s.jackson.Serialization.{read, write}

scala> implicit val formats = Serialization.formats(NoTypeHints)

scala> val ser = write(Child("Mary", 5, None))

scala> read[Child](ser)
res1: Child = Child(Mary,5,None)
Serialization supports:

Arbitrarily deep case-class graphs
All primitive types, including BigInt and Symbol
List, Seq, Array, Set and Map (note, keys of the Map must be strings: Map[String, _])
scala.Option
java.util.Date
Polymorphic Lists (see below)
Recursive types
Serialization of fields of a class (see below)
Custom serializer functions for types that are not supported (see below)
If the class contains camel-case fields (i.e: firstLetterLowercaseAndNextWordsCapitalized) but you want to produce a json string with snake casing (i.e., separated_by_underscores), you can use the snakizeKeys method:

scala> val ser = write(Person("Mary"))
ser: String = {"firstName":"Mary"}

scala> compact(render(parse(ser).snakizeKeys))
res0: String = {"first_name":"Mary"}
*/

object TunnelConfig {

  implicit val formats = Serialization.formats(NoTypeHints)

//  implicit val formats = new DefaultFormats {
//    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//  }
  def validate(cfg: SshTunnelConfig) = {
    println("validating tunnel config")
    val numPorts = cfg.ports.size
    val portNames = cfg.ports.map(_.tunnelName).toSet
    if (numPorts != portNames.size) {
      throw new RuntimeException(s"duplicate name${if (portNames.size > 1) "s" else ""}: ${portNames.mkString(",")}")
    }
  }

  def export(name: String)/*(implicit system: ActorSystem, mat: Materializer)*/: Unit = {
    val sta = SshTunnelArgs(
        tunnelName = "tun2",
        host = "host",
        serverPort = 29002,
        localPort = 29002,
        localhost = "127.0.0.1",
        key= "key",
        sshport = 2222)
    val stc = SshTunnelConfig(List(sta))
    // val json = stc.asJson
    val json = write(stc)
    //val json = stc.asJson.noSpaces

    println(json)

    IO.write(new File(name), json.toString)
//    val dest = FileIO.toPath(Paths.get(name))
//    dest.runWith(Source.single(ByteString(json.toString)))
    ()
  }

  def importConfig(name: String): SshTunnelConfig = {
    val json: String = IO.read(new File(name))
    val cfg = read[SshTunnelConfig](json)
    validate(cfg)
    cfg
//    decode[SshTunnelConfig](json) match {
//      case Right(stc) =>
//        println(s"read in $stc")
//        stc
//      case Left(e) =>
//        throw new RuntimeException(s"error decoding $e")
//    }
  }
}
