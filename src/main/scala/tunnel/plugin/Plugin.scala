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
  # Home directory of Akka, modules in the deploy directory will be loaded
  home = ""

  loglevel = "off"
  # xloglevel = "INFO"
  stdout-loglevel = "INFO"
  log-config-on-start = off

  version = "2.5.8"

  loggers = ["akka.event.slf4j.Slf4jLogger"]
  loggers-dispatcher = "akka.actor.default-dispatcher"

  # Loggers are created and registered synchronously during ActorSystem
  # start-up, and since they are actors, this timeout is used to bound the
  # waiting time
  logger-startup-timeout = 5s

  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
  #logging-filter = ""

  # Log at info level when messages are sent to dead letters.
  # Possible values:
  # on: all dead letters are logged
  # off: no logging of dead letters
  # n: positive integer, number of dead letters that will be logged
  log-dead-letters = off

  # Possibility to turn off logging of dead letters while the actor system
  # is shutting down. Logging is only done when enabled by 'log-dead-letters'
  # setting.
  log-dead-letters-during-shutdown = off

  # Toggles whether threads created by this ActorSystem should be daemons or not
  daemonic = off

  # JVM shutdown, System.exit(-1), in case of a fatal error,
  # such as OutOfMemoryError
  jvm-exit-on-fatal-error = on

  # Used to set the behavior of the scheduler.
  # Changing the default values may change the system behavior drastically so make
  # sure you know what you're doing! See the Scheduler section of the Akka
  # Documentation for more details.
  scheduler {
    # The LightArrayRevolverScheduler is used as the default scheduler in the
    # system. It does not execute the scheduled tasks on exact time, but on every
    # tick, it will run everything that is (over)due. You can increase or decrease
    # the accuracy of the execution timing by specifying smaller or larger tick
    # duration. If you are scheduling a lot of tasks you should consider increasing
    # the ticks per wheel.
    # Note that it might take up to 1 tick to stop the Timer, so setting the
    # tick-duration to a high value will make shutting down the actor system
    # take longer.
    tick-duration = 10ms

    # The timer uses a circular wheel of buckets to store the timer tasks.
    # This should be set such that the majority of scheduled timeouts (for high
    # scheduling frequency) will be shorter than one rotation of the wheel
    # (ticks-per-wheel * ticks-duration)
    # THIS MUST BE A POWER OF TWO!
    ticks-per-wheel = 512

    # This setting selects the timer implementation which shall be loaded at
    # system start-up.
    # The class given here must implement the akka.actor.Scheduler interface
    # and offer a public constructor which takes three arguments:
    #  1) com.typesafe.config.Config
    #  2) akka.event.LoggingAdapter
    #  3) java.util.concurrent.ThreadFactory
    implementation = akka.actor.LightArrayRevolverScheduler

    # When shutting down the scheduler, there will typically be a thread which
    # needs to be stopped, and this timeout determines how long to wait for
    # that to happen. In case of timeout the shutdown of the actor system will
    # proceed without running possibly still enqueued tasks.
    shutdown-timeout = 5s
  }

  # Akka installs JVM shutdown hooks by default, e.g. in CoordinatedShutdown and Artery. This property will
  # not disable user-provided hooks registered using `CoordinatedShutdown#addCancellableJvmShutdownHook`.
  # This property is related to `akka.coordinated-shutdown.run-by-jvm-shutdown-hook` below.
  # This property makes it possible to disable all such hooks if the application itself
  # or a higher level framework such as Play prefers to install the JVM shutdown hook and
  # terminate the ActorSystem itself, with or without using CoordinatedShutdown.
  jvm-shutdown-hooks = on

  actor {
    provider = "local"
    guardian-supervisor-strategy = "akka.actor.DefaultSupervisorStrategy"
    creation-timeout = 20s
    unstarted-push-timeout = 10s
    allow-java-serialization = off
    serialize-messages = off
    serialize-creators = off

    deployment {

      # deployment id pattern - on the format: /parent/child etc.
      default {

        # The id of the dispatcher to use for this actor.
        # If undefined or empty the dispatcher specified in code
        # (Props.withDispatcher) is used, or default-dispatcher if not
        # specified at all.
        dispatcher = ""

        # The id of the mailbox to use for this actor.
        # If undefined or empty the default mailbox of the configured dispatcher
        # is used or if there is no mailbox configuration the mailbox specified
        # in code (Props.withMailbox) is used.
        # If there is a mailbox defined in the configured dispatcher then that
        # overrides this setting.
        mailbox = ""

        # routing (load-balance) scheme to use
        # - available: "from-code", "round-robin", "random", "smallest-mailbox",
        #              "scatter-gather", "broadcast"
        # - or:        Fully qualified class name of the router class.
        #              The class must extend akka.routing.CustomRouterConfig and
        #              have a public constructor with com.typesafe.config.Config
        #              and optional akka.actor.DynamicAccess parameter.
        # - default is "from-code";
        # Whether or not an actor is transformed to a Router is decided in code
        # only (Props.withRouter). The type of router can be overridden in the
        # configuration; specifying "from-code" means that the values specified
        # in the code shall be used.
        # In case of routing, the actors to be routed to can be specified
        # in several ways:
        # - nr-of-instances: will create that many children
        # - routees.paths: will route messages to these paths using ActorSelection,
        #   i.e. will not create children
        # - resizer: dynamically resizable number of routees as specified in
        #   resizer below
        router = "from-code"

        # number of children to create in case of a router;
        # this setting is ignored if routees.paths is given
        nr-of-instances = 1

        # within is the timeout used for routers containing future calls
        within = 5 seconds

        # number of virtual nodes per node for consistent-hashing router
        virtual-nodes-factor = 10

        tail-chopping-router {
          # interval is duration between sending message to next routee
          interval = 10 milliseconds
        }

        routees {
          # Alternatively to giving nr-of-instances you can specify the full
          # paths of those actors which should be routed to. This setting takes
          # precedence over nr-of-instances
          paths = []
        }

        # To use a dedicated dispatcher for the routees of the pool you can
        # define the dispatcher configuration inline with the property name
        # 'pool-dispatcher' in the deployment section of the router.
        # For example:
        # pool-dispatcher {
        #   fork-join-executor.parallelism-min = 5
        #   fork-join-executor.parallelism-max = 5
        # }

        # Routers with dynamically resizable number of routees; this feature is
        # enabled by including (parts of) this section in the deployment
        resizer {

          enabled = off

          # The fewest number of routees the router should ever have.
          lower-bound = 1

          # The most number of routees the router should ever have.
          # Must be greater than or equal to lower-bound.
          upper-bound = 10

          # Threshold used to evaluate if a routee is considered to be busy
          # (under pressure). Implementation depends on this value (default is 1).
          # 0:   number of routees currently processing a message.
          # 1:   number of routees currently processing a message has
          #      some messages in mailbox.
          # > 1: number of routees with at least the configured pressure-threshold
          #      messages in their mailbox. Note that estimating mailbox size of
          #      default UnboundedMailbox is O(N) operation.
          pressure-threshold = 1

          # Percentage to increase capacity whenever all routees are busy.
          # For example, 0.2 would increase 20% (rounded up), i.e. if current
          # capacity is 6 it will request an increase of 2 more routees.
          rampup-rate = 0.2

          # Minimum fraction of busy routees before backing off.
          # For example, if this is 0.3, then we'll remove some routees only when
          # less than 30% of routees are busy, i.e. if current capacity is 10 and
          # 3 are busy then the capacity is unchanged, but if 2 or less are busy
          # the capacity is decreased.
          # Use 0.0 or negative to avoid removal of routees.
          backoff-threshold = 0.3

          # Fraction of routees to be removed when the resizer reaches the
          # backoffThreshold.
          # For example, 0.1 would decrease 10% (rounded up), i.e. if current
          # capacity is 9 it will request an decrease of 1 routee.
          backoff-rate = 0.1

          # Number of messages between resize operation.
          # Use 1 to resize before each message.
          messages-per-resize = 10
        }

        # Routers with dynamically resizable number of routees based on
        # performance metrics.
        # This feature is enabled by including (parts of) this section in
        # the deployment, cannot be enabled together with default resizer.
        optimal-size-exploring-resizer {

          enabled = off

          # The fewest number of routees the router should ever have.
          lower-bound = 1

          # The most number of routees the router should ever have.
          # Must be greater than or equal to lower-bound.
          upper-bound = 10

          # probability of doing a ramping down when all routees are busy
          # during exploration.
          chance-of-ramping-down-when-full = 0.2

          # Interval between each resize attempt
          action-interval = 5s

          # If the routees have not been fully utilized (i.e. all routees busy)
          # for such length, the resizer will downsize the pool.
          downsize-after-underutilized-for = 72h

          # Duration exploration, the ratio between the largest step size and
          # current pool size. E.g. if the current pool size is 50, and the
          # explore-step-size is 0.1, the maximum pool size change during
          # exploration will be +- 5
          explore-step-size = 0.1

          # Probabily of doing an exploration v.s. optmization.
          chance-of-exploration = 0.4

          # When downsizing after a long streak of underutilization, the resizer
          # will downsize the pool to the highest utiliziation multiplied by a
          # a downsize rasio. This downsize ratio determines the new pools size
          # in comparison to the highest utilization.
          # E.g. if the highest utilization is 10, and the down size ratio
          # is 0.8, the pool will be downsized to 8
          downsize-ratio = 0.8

          # When optimizing, the resizer only considers the sizes adjacent to the
          # current size. This number indicates how many adjacent sizes to consider.
          optimization-range = 16

          # The weight of the latest metric over old metrics when collecting
          # performance metrics.
          # E.g. if the last processing speed is 10 millis per message at pool
          # size 5, and if the new processing speed collected is 6 millis per
          # message at pool size 5. Given a weight of 0.3, the metrics
          # representing pool size 5 will be 6 * 0.3 + 10 * 0.7, i.e. 8.8 millis
          # Obviously, this number should be between 0 and 1.
          weight-of-latest-metric = 0.5
        }
      }

      /IO-DNS/inet-address {
        mailbox = "unbounded"
        router = "consistent-hashing-pool"
        nr-of-instances = 4
      }
      "/IO-DNS/inet-address/*" {
        dispatcher = "akka.actor.default-blocking-io-dispatcher"
      }
    }

    debug {
      # enable function of LoggingReceive, which is to log any received message at
      # DEBUG level
      receive = off
      autoreceive = off
      lifecycle = off
      # enable DEBUG logging of all LoggingFSMs for events, transitions and timers
      fsm = off
      # enable DEBUG logging of subscription changes on the eventStream
      event-stream = off
      # enable DEBUG logging of unhandled messages
      unhandled = off

      # enable WARN logging of misconfigured routers
      router-misconfiguration = off
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

