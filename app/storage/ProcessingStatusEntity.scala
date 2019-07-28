package storage

case class ProcessingStatusEntity(sessionId: String,
                                  status: String,
                                  timestamp: Long)

