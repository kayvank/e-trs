package kcl

import java.net.InetAddress
import java.util.UUID
import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration}
import io.circe._
import io.circe.Decoder._
import utils.Global._

sealed trait BasicAWSCredentialsProvider {

  final class BasicAWSCredentialsProvider(basic: BasicAWSCredentials) extends
    AWSCredentialsProvider {
    @Override def getCredentials: AWSCredentials = basic

    @Override def refresh = {}
  }
}

object Kcl extends BasicAWSCredentialsProvider {

  /////////////////////////////////////////////
  // there are 2 instances of this service running.
  // idletime between 2 instances = [1-8] * interval.factor
  /////////////////////////////////////////////
  val idelTimeBetweenReads = ((System.currentTimeMillis % 7) + 1
    ) * cfgVevo.getInt("kinesis.streams.endo.time.interval.factor")
  val endoStreamName =
    cfgVevo.getString("kinesis.streams.endo.name")

  val endoKclworkerId =
    s"${InetAddress.getLocalHost.getCanonicalHostName}:${cfgVevo.getString("kinesis.streams.endo.name")}:${UUID.randomUUID.toString}"

  val endoKclClient: KinesisClientLibConfiguration = new KinesisClientLibConfiguration(
    s"${cfgVevo.getString("kinesis.app.name")}-${endoStreamName}",
    endoStreamName,
    new BasicAWSCredentialsProvider(
      new BasicAWSCredentials(
        cfgVevo.getString("aws.access-key"),
        cfgVevo.getString("aws.secret-key"))),
    endoKclworkerId).withInitialPositionInStream(InitialPositionInStream.LATEST)
    .withIdleTimeBetweenReadsInMillis(idelTimeBetweenReads)
    .withInitialLeaseTableWriteCapacity(200)
    .withInitialLeaseTableReadCapacity(200)

  println(s"idletimeBetweenReads = ${idelTimeBetweenReads} ")

  val client = AmazonKinesisClientBuilder.standard()
    .withCredentials(new BasicAWSCredentialsProvider(
      new BasicAWSCredentials(
        cfgVevo.getString("aws.access-key"),
        cfgVevo.getString("aws.secret-key"))))
    .withRegion(cfgVevo.getString("kinesis.streams.telemetry.region"))
    .build()
}
