import Util.Companion.CHOICE_COUNT
import Util.Companion.createProps
import Util.Companion.printlnWithTime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import storage.Quiz
import storage.QuizQueue
import storage.Words
import kotlin.random.Random

class Background {

    private val words = Words()
    private val quizQueue = QuizQueue()
    private val quiz = Quiz()
    private val alarms = Alarm(words, quizQueue)
    private val badge = Badge(words, quizQueue)

    init {
        setMessageHandler()
        setAlarmHandler()
        setNotificationButtonHandler()
        GlobalScope.launch { badge.update() }

        extensionUpdateNotification()
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
                    "title", "New word registered",
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
        if (quizQueue.isEmpty() || words.size() < CHOICE_COUNT) {
            response(js("{}"))
        } else {
            val word = quizQueue.peek()!!
            printlnWithTime("[$word] selected for quiz")

            val choices = prepareChoices(word)
            response(createProps("word", word, "choices", choices))
            printlnWithTime("[$word] quiz choices: [$choices]")
        }
    }

    private suspend fun prepareChoices(word: String): Array<String> {
        val expected = words.translation(word)
        val choices = getChoices(expected)
        val idx = choices.indexOf(expected)

        quiz.set(word, choices, if (idx != -1) idx else CHOICE_COUNT)
        return choices
    }

    private suspend fun getChoices(expected: String): Array<String> {
        val choices = mutableSetOf<String>()
        if (Random.nextInt(2) == 0) {
            choices.add(expected)
        }
        while (choices.size < CHOICE_COUNT) {
            choices.add(words.random().translation)
        }
        return choices.toList().shuffled().toTypedArray()
    }

    // TODO [bug]: Do proper mutex. This can receive multiple "answerQuiz" when it's clicked very fast.
    //             Maybe same countermeasure for CSRF (create token for each quiz)
    private suspend fun handleQuizAnswer(request: dynamic, response: dynamic) {
        quizQueue.dequeue()
        val quizWord = quiz.get(request.word)
        val result = quizWord.expected == request.actual

        response(createProps("result", result, "expected", quizWord.expected))

        badge.update()
        words.addQuizResult(request.word, result)
        alarms.create(request.word)
        quiz.clear()
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
            when {
                notificationId.startsWith("registerWord.") -> {
                    GlobalScope.launch { removeWord(notificationId.substringAfter(".")) }
                }
                notificationId == "extensionUpdate" -> {
                    chrome.tabs.create(createProps("url", changelogUrl()))
                }
                else -> {
                }
            }
        }
    }

    private fun changelogUrl(): String {
        return "https://konohiroaki.github.io/quiz-for-autogenerated-vocabulary-list/CHANGELOG.html"
    }

    private fun extensionUpdateNotification() {
        chrome.runtime.onInstalled.addListener { details ->
            if (details.reason == "update") {
                val version = chrome.runtime.getManifest().version
                chrome.notifications.create(
                    "extensionUpdate", createProps(
                        "type", "basic",
                        "iconUrl", "icon128.png",
                        "title", "[$version] Extension Updated",
                        "message", "",
                        "buttons", arrayOf(createProps("title", "Check out Changelog!"))
                    )
                )
            }
        }
    }
}
