package utilities

/**
 * Created by cnavarro on 20/10/16.
 */
trait DiscoveryService {
  def getIpAndPort(serviceId: String) : (String, Int)
}
