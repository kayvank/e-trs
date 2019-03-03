package kcl 

import collection.JavaConverters._
import java.nio.ByteBuffer
import com.amazonaws.services.kinesis.model._

object KplBatchProtocol {

  import KplModle._

  implicit class PutRecrodSRequestProtocol(kplRecords: Vector[KplRecord]) {
    def asPutRecordSRequest: PutRecordsRequest = {
      val putrecsList =
        kplRecords.foldLeft(Vector[PutRecordsRequestEntry]())((z,r) =>
          (new PutRecordsRequestEntry)
            .withData(r.data)
            .withPartitionKey(r.partitionKey) +: z)

      (new PutRecordsRequest())
        .withStreamName(kplRecords.head.streamName)
        .withRecords(putrecsList.asJava)
    }
  }
  implicit class PutRecrodRequestProtocol(kplRecord: KplRecord) {
    def asPutRecordRequest = {
      (new PutRecordRequest)
        .withData(kplRecord.data)
        .withStreamName(kplRecord.streamName)
      .withPartitionKey(kplRecord.streamName)
    }
  }
}
