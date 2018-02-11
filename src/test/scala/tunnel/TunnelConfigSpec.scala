package tunnel.plugin

import org.scalatest._

class TunnelConfigSpec extends FlatSpec with Matchers {

  "A Stack" should "export json" in {
    TunnelConfig.export("target/tc.json")
  }

  it should "import json" in {
    val c = TunnelConfig.importConfig("src/test/resources/tunnel-config.json")
    println(s"c = $c")
  }
}