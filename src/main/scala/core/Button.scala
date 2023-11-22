package core

import com.bot4s.telegram.methods.{EditMessageReplyMarkup, SendMessage}
import com.bot4s.telegram.models.{CallbackQuery, ChatId, InlineKeyboardButton, ReplyMarkup}
import core.Main.{onCallbackWithTag, prefixTag, request}

import scala.concurrent.{ExecutionContext, Future}

case class NextMessageParams(text: String, newMarkup: Option[ReplyMarkup] = None)

case class Button(
                   tittle: String,
                   actionOnPressed: CallbackQuery => Future[Unit],
                   newMarkup: Option[ReplyMarkup] = None,
                   newMessageParams: Option[NextMessageParams]
                 )(implicit ec: ExecutionContext) {
  private val tag: String = tittle.toLowerCase.replaceAll("[\\p{Punct}\\s]", "_")

  def getInlineKeyboardButton: InlineKeyboardButton = InlineKeyboardButton.callbackData(tittle, prefixTag(tag)(""))

  onCallbackWithTag(tag) {
    implicit cbq: CallbackQuery =>
      actionOnPressed(cbq).flatTap(request(
        EditMessageReplyMarkup(
          Some(ChatId(cbq.from.id)),
          cbq.message.map(_.messageId),
          replyMarkup = None
        )
      )).flatTap(
        newMessageParams.map(nmp => request(SendMessage(cbq.from.id, nmp.text, replyMarkup = nmp.newMarkup)))
      )
  }

}
