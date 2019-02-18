package app

import common.graphql.UserContext
import common.slick.SchemaInitializer
import core.guice.injection.InjectorProvider._
import graphql.schema.{JwtSchema, UserSchema}
import repositories.{UserProfileSchemaInitializer, UserSchemaInitializer}
import sangria.schema.Field
import shapes.ServerModule

import scala.collection.mutable

class UserModule extends ServerModule[UserContext, SchemaInitializer[_]] {

  lazy val userSchema: UserSchema = inject[UserSchema]
  lazy val jwtSchema: JwtSchema = inject[JwtSchema]
  lazy val userSchemaInitializer: UserSchemaInitializer = inject[UserSchemaInitializer]
  lazy val userProfileSchemaInitializer: UserProfileSchemaInitializer = inject[UserProfileSchemaInitializer]

  override lazy val slickSchemas: mutable.HashSet[SchemaInitializer[_]] =
    mutable.HashSet(userSchemaInitializer, userProfileSchemaInitializer)
  override lazy val queries: mutable.HashSet[Field[UserContext, Unit]] = mutable.HashSet(userSchema.queries: _*)
  override lazy val mutations: mutable.HashSet[Field[UserContext, Unit]] =
    mutable.HashSet(jwtSchema.mutations ++ userSchema.mutations: _*)
}
