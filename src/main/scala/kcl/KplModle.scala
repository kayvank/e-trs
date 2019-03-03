package kcl

import utils.Global._
import java.nio.ByteBuffer

object KplModle {

  case class KplRecord(
    data: ByteBuffer,
    partitionKey: String =
      System.currentTimeMillis.toString,
    streamName: String =
      cfgVevo.getString("kinesis.streams.telemetry.name")
  )

  case class PublishResult(
    errors: Int,
    totalRecords: Int
  )

}
