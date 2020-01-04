import Util.Companion.createProps
import Util.Companion.select
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.js.Date

class WordList {

    companion object {

        // TODO [feature]: show result after pressing buttons.
        //                 e.g. add to alarm -> show what time the alarm will ring.
        fun update(words: Array<dynamic>, alarms: Array<dynamic>, quizQueue: Array<dynamic>) {
            select<HTMLDivElement>("#words").replaceWith(document.create.div {
                id = "words"
                words.forEach { word ->
                    val alarm = alarms.firstOrNull { it.name == word.word }
                    val inQueue = quizQueue.contains(word.word)
                    wordListItem(word, alarm, inQueue)
                }
            })
        }

        private fun DIV.wordListItem(word: dynamic, alarm: dynamic, inQueue: Boolean) {
            div("list-group-item list-group-item-action") {
                titleAndStatus(word.word, alarm, inQueue)
                div { +(word.translation as String) }
                div { +(word.quizResult as Array<String>).joinToString(prefix = "[", postfix = "]") }
                buttonList(word.word)
                changeTranslation(word.word)
            }
        }

        private fun DIV.titleAndStatus(word: String, alarm: dynamic, inQueue: Boolean) {
            val status = when {
                alarm != null -> "Enqueue at ${Date(alarm.scheduledTime as Double).toLocaleString()}"
                inQueue -> "In Queue"
                else -> "Not active"
            }
            div("d-flex w-100 justify-content-between") {
                h5 { +word }
                small { +status }
            }
        }

        private fun DIV.buttonList(word: String) {
            div("list-group list-group-horizontal") {
                buttonListItem(word, "addToQueue", "fas fa-plus-square")
                buttonListItem(word, "removeFromQueue", "fas fa-minus-square")
                buttonListItem(word, "addToAlarm", "fas fa-bell")
                buttonListItem(word, "removeFromAlarm", "fas fa-bell-slash")
                buttonListItem(word, "removeFromWordList", "fas fa-trash-alt")
            }
        }

        private fun DIV.buttonListItem(word: String, msgType: String, fontAwesomeIcon: String) {
            button(classes = "list-group-item list-group-item-action text-center") {
                style = "outline:none;"
                i(fontAwesomeIcon)
                onClickFunction = {
                    chrome.runtime.sendMessage(null, createProps("msgType", msgType, "word", word))
                }
            }
        }

        private fun DIV.changeTranslation(word: String) {
            div("row m-0") {
                input(type = InputType.text, classes = "col-6 form-control") {
                    id = "$word-changeTranslation"
                }
                button(classes = "ml-auto col-5 btn btn-secondary") {
                    +"change translation"
                    onClickFunction = {
                        // TODO [investigation]: Is there no smarter way to read input field value?
                        val translation = document.querySelector("#$word-changeTranslation") as HTMLInputElement
                        if (translation.value != "") {
                            chrome.runtime.sendMessage(null, changeTranslationRequest(word, translation.value))
                        }
                    }
                }
            }
        }

        private fun changeTranslationRequest(word: String, newTranslation: String): dynamic {
            return createProps(
                "msgType", "changeTranslation",
                "word", word,
                "translation", newTranslation
            )
        }
    }
}
