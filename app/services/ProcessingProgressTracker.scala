package services

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.QueueOfferResult.{Dropped, Enqueued, Failure, QueueClosed}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import models.ProcessingStatus

import scala.collection.mutable
import scala.concurrent.Future

object ProcessingProgressTracker {

  implicit private val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit private val system: ActorSystem = ActorSystem("ProcessingProgressTracker")
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  private val sourceOfStatusesPerSession = mutable.HashMap.empty[String, Source[ProcessingStatus.Status, NotUsed]]
  private val sourceQueueOfStatusesPerSession = mutable.HashMap.empty[String, SourceQueueWithComplete[ProcessingStatus.Status]]

  def getSourceOfStatuses(sessionId: String): Option[Source[ProcessingStatus.Status, NotUsed]] =
    sourceOfStatusesPerSession.get(sessionId)

  // it's configured to keep the source open for 30s (reusing it for consecutive requests)
  def createSourceOfStatuses(sessionId: String): Source[ProcessingStatus.Status, NotUsed] = {
    val initialSourceOfStatuses = Source.queue[ProcessingStatus.Status](100, OverflowStrategy.dropHead)
    val sourceQueueOfStatuses: (SourceQueueWithComplete[ProcessingStatus.Status], Source[ProcessingStatus.Status, NotUsed]) =
      initialSourceOfStatuses.preMaterialize()
    sourceQueueOfStatusesPerSession.update(sessionId, sourceQueueOfStatuses._1)
    sourceOfStatusesPerSession.update(sessionId, sourceQueueOfStatuses._2)

    // watch for closing the source queue (probably because of being inactive) and at that point remove it
    // and its consumer source from the maps
    sourceQueueOfStatuses._1.watchCompletion().foreach { _ =>
      sourceQueueOfStatusesPerSession.remove(sessionId)
        .foreach(_ => println(s"Source queue with session id $sessionId removed from sourceQueueOfStatusesPerSession"))
      sourceOfStatusesPerSession.remove(sessionId)
        .foreach(_ => println(s"Source with session id $sessionId removed from sourceOfStatusesPerSession"))
    }

    sourceQueueOfStatuses._2
  }

  def registerNewStatus(status: ProcessingStatus.Status, sessionId: UUID): Future[Unit] = {
    val runningSourceQueueOfStatusesO: Option[SourceQueueWithComplete[ProcessingStatus.Status]] =
      sourceQueueOfStatusesPerSession.get(sessionId.toString).orElse {
        createSourceOfStatuses(sessionId.toString)
        sourceQueueOfStatusesPerSession.get(sessionId.toString)
      }

    runningSourceQueueOfStatusesO match {
      case Some(runningSourceQueueOfStatuses) =>
        // adding a status through the method `offer()` triggers passing the status to `sourceOfStatuses`, from which
        // the consumer is consuming the statuses
        runningSourceQueueOfStatuses.offer(status)
          .flatMap {
            case Enqueued =>
              Future.successful(())
            case Dropped =>
              Future.failed(new Exception(s"Status: $status couldn't be published"))
            case Failure(e) =>
              println("Failure(e):" + e.getMessage)
              Future.failed(new Exception(s"Status: $status couldn't be published because of $e"))
            case QueueClosed =>
              println("QueueClosed")
              Future.failed(new Exception(s"Status: $status couldn't be published because the publishing queue was closed"))
          }.recover({
          case t =>
            println(t.getMessage)
        })
      case None => Future.failed(new Exception("Source of statuses was already closed"))
    }

  }
}
