package tunnel.plugin

case class SshTunnelArgs(
  tunnelName: String,
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
