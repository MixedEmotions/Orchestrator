package utilities

/**
 * Created by cnavarro on 14/11/16.
 */
trait RequestExecutor {

  def executeRequest(method: String, query: String, requestTimeout: Int = 50000, requestDelay: Int = 500, body: Option[String],
                     fileUploadData: Option[Map[String,String]]=None, contentType: String): String

}
