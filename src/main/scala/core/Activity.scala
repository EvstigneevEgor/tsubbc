package core

import com.bot4s.telegram.models.{Contact, InlineKeyboardButton}
import core.Main.prefixTag
import date_base.User

trait Activity {
  val messageId: Option[Int]
  val user: User
}

case class ButtonPressed(button: Button, override val user: User, override val messageId: Option[Int]) extends Activity

case class MessageReceive(text: String, override val user: User, override val messageId: Option[Int]) extends Activity

case class ContactReceive(contact: Contact, override val user: User, override val messageId: Option[Int]) extends Activity

object Activity

case class Button(
                   tittle: String) {
  val tag: String = tittle.toLowerCase.replaceAll("[\\p{Punct}\\s]", "_")

  def getInlineKeyboardButton: InlineKeyboardButton = InlineKeyboardButton.callbackData(tittle, prefixTag(tag)(""))
}