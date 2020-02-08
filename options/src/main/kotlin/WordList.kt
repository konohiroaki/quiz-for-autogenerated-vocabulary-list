import Util.Companion.createProps
import Util.Companion.printlnWithTime
import Util.Companion.select
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.js.Date
import kotlin.random.Random

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

        fun updateV2(words: Array<dynamic>, alarms: Array<dynamic>, quizQueue: Array<String>) {
            select<HTMLDivElement>("#words").replaceWith(document.create.div {
                id = "words"
                for (word in words) {
                    val alarm = alarms.firstOrNull { it.name == word.wordKey }
                    val inQueue = quizQueue.contains(word.wordKey)
                    wordListItemV2(word, alarm, inQueue)
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

        private fun DIV.wordListItemV2(word: dynamic, alarm: dynamic, inQueue: Boolean) {
            div("list-group-item list-group-item-action") {
                printlnWithTime(JSON.stringify(word))
                titleAndStatusV2(word.wordKey, alarm, inQueue)
                div { +(word.translation as String) }
                div { +("correctCount: ${word.correctCount}") }
                buttonListV2(word.wordKey)
                changeTranslationV2(word.wordKey)
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

        private fun DIV.titleAndStatusV2(wordKey: String, alarm: dynamic, inQueue: Boolean) {
            val status = when {
                alarm != null -> "Enqueue at ${Date(alarm.scheduledTime as Double).toLocaleString()}"
                inQueue -> "In Queue"
                else -> "Not active"
            }
            printlnWithTime(wordKey)
            div("d-flex w-100 justify-content-between") {
                h5 { +wordKey }
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

        private fun DIV.buttonListV2(wordKey: String) {
            div("list-group list-group-horizontal") {
                buttonListItemV2(wordKey, "addToQueue", "fas fa-plus-square")
                buttonListItemV2(wordKey, "removeFromQueue", "fas fa-minus-square")
                buttonListItemV2(wordKey, "addToAlarm", "fas fa-bell")
                buttonListItemV2(wordKey, "removeFromAlarm", "fas fa-bell-slash")
                buttonListItemV2(wordKey, "removeFromWordList", "fas fa-trash-alt")
            }
        }

        private fun DIV.buttonListItem(word: String, msgType: String, fontAwesomeIcon: String) {
            button(classes = "list-group-item list-group-item-action text-center") {
                style = "outline:none;"
                i(fontAwesomeIcon)
                title = msgType.replace("([A-Z])".toRegex(), " $1").toLowerCase()
                onClickFunction = {
                    chrome.runtime.sendMessage(null, createProps("msgType", msgType, "word", word))
                }
            }
        }

        private fun DIV.buttonListItemV2(wordKey: String, msgType: String, fontAwesomeIcon: String) {
            button(classes = "list-group-item list-group-item-action text-center") {
                style = "outline:none;"
                i(fontAwesomeIcon)
                title = msgType.replace("([A-Z])".toRegex(), " $1").toLowerCase()
                onClickFunction = {
                    chrome.runtime.sendMessage(null, createProps("msgType", msgType, "wordKey", wordKey))
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

        private fun DIV.changeTranslationV2(wordKey: String) {
            val charPool = ('A'..'Z') + ('0'..'9')
            val randomString = (1..8).map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
            div("row m-0") {
                input(type = InputType.text, classes = "col-6 form-control") {
                    id = "changeTranslation-$randomString"
                }
                button(classes = "ml-auto col-5 btn btn-secondary") {
                    +"change translation"
                    onClickFunction = {
                        val translation = document.querySelector("#changeTranslation-$randomString") as HTMLInputElement
                        if (translation.value != "") {
                            chrome.runtime.sendMessage(null, changeTranslationRequestV2(wordKey, translation.value))
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

        private fun changeTranslationRequestV2(wordKey: String, newTranslation: String): dynamic {
            return createProps(
                "msgType", "changeTranslation",
                "wordKey", wordKey,
                "translation", newTranslation
            )
        }
    }
}
