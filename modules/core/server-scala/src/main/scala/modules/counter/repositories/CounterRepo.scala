package modules.counter.repositories

import com.google.inject.Inject
import common.errors.InternalServerError
import javax.inject.Singleton
import modules.counter.models.Counter
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CounterRepo {

  def inc(counter: Counter): Future[Counter]

  def find(id: Int): Future[Counter]
}

@Singleton
class CounterRepoImpl @Inject()(db: Database) extends CounterRepo {

  val query: TableQuery[Counter.Table] = TableQuery[Counter.Table]

  override def inc(counter: Counter): Future[Counter] = db.run(Actions.inc(counter))

  override def find(id: Int): Future[Counter] = db.run(Actions.find(id))

  object Actions {

    def inc(counter: Counter): DBIO[Counter] = (
      for {
        result <- query.filter(_.id === counter.id).result
        foundCounter <- if (result.lengthCompare(1) == 0) DBIO.successful(result.head) else DBIO.failed(InternalServerError())
        newCounter = foundCounter.copy(amount = foundCounter.amount + counter.amount)
        _ <- query.update(newCounter)
      } yield newCounter
      ).transactionally

    def find(id: Int): DBIO[Counter] = {
      for {
        result <- query.filter(_.id === id).result
        foundCounter <- if (result.lengthCompare(1) == 0) DBIO.successful(result.head) else DBIO.failed(InternalServerError())
      } yield foundCounter
    }
  }

}