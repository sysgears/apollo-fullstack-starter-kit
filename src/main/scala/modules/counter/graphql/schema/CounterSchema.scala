package modules.counter.graphql.schema

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import common.Logger
import common.graphql.DispatcherResolver._
import core.graphql.{GraphQLSchema, UserContext}
import core.guice.injection.GuiceActorRefProvider
import core.services.publisher.PubSubService
import javax.inject.Inject
import modules.counter.graphql.resolvers.CounterResolver
import modules.counter.models.Counter
import modules.counter.services.count.CounterActor.GetAmount
import sangria.macros.derive.{ExcludeFields, ObjectTypeName, deriveObjectType}
import sangria.schema.{Action, Argument, Field, IntType, ObjectType}
import sangria.streaming.akkaStreams._
import core.services.publisher.RichPubSubService._

import scala.concurrent.ExecutionContext

class CounterSchema @Inject()(implicit val pubsubService: PubSubService[Counter],
                              materializer: ActorMaterializer,
                              actorSystem: ActorSystem,
                              executionContext: ExecutionContext) extends GraphQLSchema
  with Logger {

  object Types {
    implicit val counter: ObjectType[Unit, Counter] = deriveObjectType(ObjectTypeName("Counter"), ExcludeFields("id"))
  }

  override def queries: List[Field[UserContext, Unit]] = List(
    Field(
      name = "serverCounter",
      fieldType = Types.counter,
      resolve = sc => resolveWithDispatcher[Counter](
        input = GetAmount,
        userContext = sc.ctx,
        onException = _ => Counter(amount = 0),
        namedResolverActor = CounterResolver
      )
    )
  )

  override def mutations: List[Field[UserContext, Unit]] = List(
    Field(
      name = "addServerCounter",
      fieldType = Types.counter,
      arguments = Argument(name = "amount", argumentType = IntType) :: Nil,
      resolve = sc => {
        val amount = sc.args.arg[Int]("amount")
        resolveWithDispatcher[Counter](
          input = amount,
          userContext = sc.ctx,
          onException = _ => Counter(amount = 0),
          namedResolverActor = CounterResolver
        ).pub
      }
    )
  )

  override def subscriptions: List[Field[UserContext, Unit]] = List(
    Field.subs(
      name = "counterUpdated",
      fieldType = Types.counter,
      resolve = _ => pubsubService.subscribe
    )
  )
}