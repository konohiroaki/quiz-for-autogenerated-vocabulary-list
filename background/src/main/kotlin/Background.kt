import Util.Companion.createProps
import Util.Companion.printlnWithTime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import storage.QuizQueue
import storage.Words

fun main() {
    printlnWithTime("reloaded")
    Background()
}

class Background {

    private val words = Words()
    private val quizQueue = QuizQueue()
    private val alarms = Alarm(words, quizQueue)
    private val badge = Badge(words, quizQueue)

    init {
        setMessageHandler()
        setAlarmHandler()
        setNotificationButtonHandler()
        GlobalScope.launch { badge.update() }
    }

    private fun setMessageHandler() {
        chrome.runtime.onMessage.addListener { request, _, response ->
            GlobalScope.launch {
                when (request.msgType) {
                    // Intended for Content Script
                    "registerWord" -> registerWord(request)

                    // Intended for BrowserAction
                    "requestQuiz" -> respondNextQuiz(response)
                    "answerQuiz" -> handleQuizAnswer(request, response)

                    // Intended for Option page
                    "addToQueue" -> addToQueue(request.word)
                    "removeFromQueue" -> removeWordFromQueue(request.word)
                    "addToAlarm" -> alarms.create(request.word)
                    "removeFromAlarm" -> alarms.remove(request.word)
                    "removeFromWordList" -> removeWord(request.word)
                    "changeTranslation" -> words.changeTranslation(request.word, request.translation)
                    "getAllData" -> getAllData(response)
                    "wipeOutData" -> wipeOutData()
                }
            }
            // https://stackoverflow.com/a/20077854/6642042
            js("return true")
        }
    }

    private suspend fun registerWord(request: dynamic) {
        if (words.add(request.word, request.translation)) {
            chrome.notifications.create(
                "registerWord.${request.word}", createProps(
                    "type", "basic",
                    "iconUrl", "icon128.png",
                    "title", "[Vocab-Anki-Push] New word",
                    "message", "[${request.word}] -> ${request.translation}",
                    "buttons", arrayOf(createProps("title", "Cancel word registration"))
                )
            )
            alarms.create(request.word)
        }
    }

    private suspend fun respondNextQuiz(response: dynamic) {
        printlnWithTime("quiz requested")
        printlnWithTime("quizQueue-> ${quizQueue.toJsonString()}")
        if (quizQueue.isEmpty()) {
            response(js("{}"))
        } else {
            if (words.size() < 4) {
                response(js("{}"))
            } else {
                val word = quizQueue.peek()!!
                printlnWithTime("[$word] selected for quiz")
                val choices = getChoices(word, 4)
                response(createProps("word", word, "choices", choices))
                printlnWithTime("[$word] quiz choices: [$choices]")
            }
        }
    }

    private suspend fun getChoices(word: String, count: Int): Array<String> {
        val choices = mutableSetOf<String>(words.translation(word))
        while (choices.size < count) {
            choices.add(words.random().translation)
        }
        return choices.toList().shuffled().toTypedArray()
    }

    // TODO [bug]: Do proper mutex. This can receive multiple "answerQuiz" when it's clicked very fast.
    //             Maybe same countermeasure for CSRF (create token for each quiz)
    private suspend fun handleQuizAnswer(request: dynamic, response: dynamic) {
        quizQueue.dequeue()
        badge.update()
        val result = words.addQuizResult(request.word, request.choice)
        alarms.create(request.word)
        response(createProps("result", result, "answer", words.translation(request.word)))
    }

    private suspend fun getAllData(response: dynamic) {
        val words = words.getWordsAsArray()
        val quizQueue = quizQueue.getQuizQueue()
        val alarms = alarms.getAll()

        response(createProps("words", words, "quizQueue", quizQueue, "alarms", alarms))
    }

    private suspend fun removeWord(word: String) {
        alarms.remove(word)
        quizQueue.remove(word)
        words.remove(word)
        badge.update()
    }

    private suspend fun removeWordFromQueue(word: String) {
        alarms.remove(word)
        quizQueue.remove(word)
        badge.update()
    }

    private suspend fun addToQueue(word: String) {
        if (alarms.contains(word)) {
            alarms.remove(word)
            printlnWithTime("[$word] removed from alarm")
        }
        if (!quizQueue.contains(word)) {
            quizQueue.enqueue(word)
            badge.update()
            printlnWithTime("[$word] added in queue")
        }
    }

    private suspend fun wipeOutData() {
        chrome.alarms.clearAll { chrome.storage.sync.clear { GlobalScope.launch { badge.update() } } }
    }

    private fun setAlarmHandler() {
        chrome.alarms.onAlarm.addListener { alarm ->
            GlobalScope.launch {
                printlnWithTime("[${alarm.name}] alarm ringed")
                quizQueue.enqueue(alarm.name)
                badge.update()
            }
        }
    }

    private fun setNotificationButtonHandler() {
        chrome.notifications.onButtonClicked.addListener { notificationId: String, _ ->
            // notificationId currently always "registerWord.$word"
            GlobalScope.launch {
                removeWord(notificationId.substringAfter("."))
            }
        }
    }
}
