package modules.upload.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern._
import com.google.inject.Inject
import common.ActorNamed
import common.DatabaseExecutor._
import modules.upload.actors.FileActor.SaveFileMetadata
import modules.upload.models.FileMetadata
import modules.upload.repositories.FileMetadataRepository

import scala.concurrent.ExecutionContext

object FileActor extends ActorNamed {

  case class SaveFileMetadata(file: FileMetadata, sender: ActorRef)

  final val name = "FileActor"
}

class FileActor @Inject()(fileMetadataRepository: FileMetadataRepository)
                         (implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case saveFileMetadata: SaveFileMetadata =>
      log.info(s"Received a message: [ $saveFileMetadata ]")
      fileMetadataRepository.save(saveFileMetadata.file).run.pipeTo(saveFileMetadata.sender)
  }
}