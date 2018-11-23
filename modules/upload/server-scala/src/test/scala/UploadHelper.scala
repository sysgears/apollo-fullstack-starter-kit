import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import core.controllers.graphql.GraphQLController
import core.guice.injection.Injecting
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpec}
import repositories.FileSchemaInitializer

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait UploadHelper extends WordSpec
  with ScalatestRouteTest
  with BeforeAndAfter
  with BeforeAndAfterAll
  with Injecting
  with Matchers {

  val endpoint: String = "/graphql"
  val routes: Route = inject[GraphQLController].routes

  val fileInitializer: FileSchemaInitializer = inject[FileSchemaInitializer]

  before {
    clean()
    dropDb()
    initDb()
  }

  after {
    dropDb()
    clean()
  }

  def clean(): Unit = ()

  private def initDb(): Unit = {
    await(fileInitializer.create())
  }

  private def dropDb(): Unit = {
    await(fileInitializer.drop())
  }

  def await[T](asyncFunc: => Future[T]): T = Await.result[T](asyncFunc, Duration.Inf)
}