package model

import io.circe.Json

sealed trait Domain

case class Endo(
  event_id: String,
  metadata: Json,
  device_context: Json
)
case class EndoTelemetry(
  endo: Endo,
  custom: Json
) extends Domain


case class ProgramResult(kclPublish: Int) extends Domain
case class KclPublishResult(
  errors: Int,
  totalRecords: Int
)
