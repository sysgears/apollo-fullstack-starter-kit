package model

import common.FieldError
import jwt.model.Tokens

case class AuthPayload(
    user: Option[User] = None,
    tokens: Option[Tokens] = None,
    errors: Option[List[FieldError]] = None
)
