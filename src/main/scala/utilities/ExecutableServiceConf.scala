package utilities

/**
 * Created by cnavarro on 2/11/16.
 */
case class ExecutableServiceConf(requestUrl: String, method: String, body: Option[String], outputField:String, responsePath: Option[String],
                            responseMap: Option[Map[String,String]], deleteString: Option[String], requestDelayMs: Int, requestTimeoutMs: Int,
                            fileUploadConf: Option[Map[String, String]], responseParseString: Option[String], pivotPath: Option[String],
                                  pivotName: Option[String], pivotId: Option[String], requirementField: Option[String], requirementRegex: Option[String], pollingCondition: Option[String]) {

}
