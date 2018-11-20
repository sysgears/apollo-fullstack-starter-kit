package modules.contact.graphql.resolvers

import com.github.jurajburian.mailer.{Content, Message}
import com.typesafe.config.Config
import common.ActorUtil
import javax.inject.Inject
import javax.mail.internet.InternetAddress
import modules.contact.models.{Contact, ContactPayload}
import modules.mail.service.ethereal.EtherealMailService

import scala.concurrent.{ExecutionContext, Future}

class ContactResolverImpl @Inject()(mailService: EtherealMailService,
                                    config: Config)
                                   (implicit executionContext: ExecutionContext) extends ContactResolver
  with ActorUtil {

  override def sendMail(contact: Contact): Future[ContactPayload] =
    mailService.sent(
      Message(
        subject = "New email through contact us page",
        content = Content().html(s"<p>${contact.name} is sending the following message.</p><p>${contact.content}</p>"),
        from = new InternetAddress(contact.email),
        to = Seq(new InternetAddress(config.getString("email.ethereal.user")))
      )
    ).map(mailPayload => ContactPayload(mailPayload.errors))
}