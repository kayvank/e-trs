package model

sealed trait Ex

final case class KclRecordToStringException(message: String)
    extends Exception(message) with Ex

final case class KclException(message: String)
    extends Exception(message) with Ex

final case class KclToTelemetryException(message: String)
    extends Exception(message) with Ex

final case class JsonParsingException(message: String)
    extends Exception(message) with Ex

final case class NonApplicationException(message: String)
    extends Exception(message) with Ex


