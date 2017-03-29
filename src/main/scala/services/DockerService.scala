package services

import utilities.{RequestExecutor, ExecutableServiceConf, DiscoveryService, MarathonDiscoveryService}


class DockerService(serviceName: String, serviceId: String, serviceDiscovery: DiscoveryService, serviceConf: ExecutableServiceConf, requestExecutor: RequestExecutor)
extends ExecutableService(serviceId, serviceConf, requestExecutor ){

  def getIpAndPort(): (String, Int) = {
    serviceDiscovery.getIpAndPort(serviceId)
  }

}
