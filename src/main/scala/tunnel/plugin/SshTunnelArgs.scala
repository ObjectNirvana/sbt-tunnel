package tunnel.plugin

case class SshTunnelArgs(
  tunnelName: String,
  host: String,
  user: Option[String],
  serverPort: Int,
  localPort: Int,
  localhost: String,
  key: Option[String],
  sshport: Option[Int],
  sshcmdx: Option[String] = None) {
  def keyArg: String = key.map(key => s"-i ${key}").getOrElse("")
  def sshPortArg: String = sshport.map(sshport => s"-p ${sshport}").getOrElse("")
  def userArg: String = user.map(user => s"${sshport}@").getOrElse("")
  def sshcmd = sshcmdx.getOrElse(s"""ssh -v ${keyArg} ${sshPortArg} ${userArg}${host} -L $serverPort:${localhost}:$localPort -N""")
}

case class SshTunnelConfig(
    ports: List[SshTunnelArgs])
