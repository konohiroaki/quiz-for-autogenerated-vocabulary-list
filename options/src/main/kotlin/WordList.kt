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
        fun update(words: Array<dynamic>, alarms: Array<dynamic>, quizQueue: Array<String>) {
            select<HTMLDivElement>("#words").replaceWith(document.create.div {
                id = "words"
                for (word in words) {
                    val alarm = alarms.firstOrNull { it.name == word.wordKey }
                    val inQueue = quizQueue.contains(word.wordKey)
                    wordListItem(word, alarm, inQueue)
                }
            })
        }

        private fun DIV.wordListItem(word: dynamic, alarm: dynamic, inQueue: Boolean) {
            div("list-group-item list-group-item-action") {
                printlnWithTime(JSON.stringify(word))
                languageAndStatus(word.wordKey, alarm, inQueue)
                titleAndCorrectCount(word.wordKey, word.correctCount)
                div { +(word.translation as String) }
                buttonList(word.wordKey)
                changeTranslation(word.wordKey)
            }
        }

        private fun DIV.languageAndStatus(wordKey: String, alarm: dynamic, inQueue: Boolean) {
            val status = when {
                alarm != null -> "Enqueue at ${Date(alarm.scheduledTime as Double).toLocaleString()}"
                inQueue -> "In Queue"
                else -> "Not active"
            }
            div("d-flex w-100 justify-content-between") {
                small { +"${Languages.getSrcLang(wordKey)} ➤ ${Languages.getDstLang(wordKey)}" }
                small { +status }
            }
        }

        private fun DIV.titleAndCorrectCount(wordKey: String, correctCount: Int) {
            div("d-flex w-100 justify-content-between") {
                h5 { +(Languages.getWord(wordKey)) }
                div { +("☑ $correctCount") }
            }
        }

        private fun DIV.buttonList(wordKey: String) {
            div("list-group list-group-horizontal") {
                buttonListItem(wordKey, "addToQueue", "fas fa-plus-square")
                buttonListItem(wordKey, "removeFromQueue", "fas fa-minus-square")
                buttonListItem(wordKey, "addToAlarm", "fas fa-bell")
                buttonListItem(wordKey, "removeFromAlarm", "fas fa-bell-slash")
                buttonListItem(wordKey, "removeFromWordList", "fas fa-trash-alt")
            }
        }

        private fun DIV.buttonListItem(wordKey: String, msgType: String, fontAwesomeIcon: String) {
            button(classes = "list-group-item list-group-item-action text-center") {
                style = "outline:none;"
                i(fontAwesomeIcon)
                title = msgType.replace("([A-Z])".toRegex(), " $1").toLowerCase()
                onClickFunction = {
                    chrome.runtime.sendMessage(null, createProps("msgType", msgType, "wordKey", wordKey))
                }
            }
        }

        private fun DIV.changeTranslation(wordKey: String) {
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
                            chrome.runtime.sendMessage(null, changeTranslationRequest(wordKey, translation.value))
                        }
                    }
                }
            }
        }

        private fun changeTranslationRequest(wordKey: String, newTranslation: String): dynamic {
            return createProps(
                "msgType", "changeTranslation",
                "wordKey", wordKey,
                "translation", newTranslation
            )
        }
    }
}
