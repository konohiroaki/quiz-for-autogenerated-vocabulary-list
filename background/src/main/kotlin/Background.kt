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
//        setMessageHandler()
        setMessageHandlerV2()
        setAlarmHandler()
        setNotificationButtonHandler()
        GlobalScope.launch { badge.update() }
//        GlobalScope.launch { badge.updateV2() }

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

    private fun setMessageHandlerV2() {
        chrome.runtime.onMessage.addListener { request, _, response ->
            GlobalScope.launch {
                when (request.msgType) {
                    // Intended for Content Script
                    "registerWord" -> registerWordV2(request)

                    // Intended for BrowserAction
                    "requestQuiz" -> respondNextQuizV2(response)
                    "answerQuiz" -> handleQuizAnswerV2(request, response)

                    // Intended for Option page
                    "addToQueue" -> addToQueue(request.wordKey)
                    "removeFromQueue" -> removeWordFromQueue(request.wordKey)
                    "addToAlarm" -> alarms.createV2(request.wordKey)
                    "removeFromAlarm" -> alarms.remove(request.wordKey)
                    "removeFromWordList" -> removeWord(request.wordKey)
                    "changeTranslation" -> words.changeTranslationV2(request.wordKey, request.translation)
                    "getAllData" -> getAllDataV2(response)
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

    private suspend fun registerWordV2(request: dynamic) {
        if (words.addV2(request.wordKey, request.translation)) {
            chrome.notifications.create(
                "registerWord.${request.wordKey}", createProps(
                    "type", "basic",
                    "iconUrl", "icon128.png",
                    "title", "New word registered",
                    "message", "[${request.wordKey}] -> ${request.translation}",
                    "buttons", arrayOf(createProps("title", "Cancel word registration"))
                )
            )
            alarms.createV2(request.wordKey)
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

    private suspend fun respondNextQuizV2(response: dynamic) {
        printlnWithTime("quiz requested")
        printlnWithTime("quizQueue-> ${quizQueue.toJsonString()}")
        // TODO [bug]: need to check only same dstLang
        if (quizQueue.isEmpty() || words.sizeV2(null) < CHOICE_COUNT) {
            response(js("{}"))
        } else {
            val wordKey = quizQueue.peek()!!
            printlnWithTime("[$wordKey] selected for quiz")

            val choices = prepareChoicesV2(wordKey)
            response(createProps("wordKey", wordKey, "choices", choices))
            printlnWithTime("[$wordKey] quiz choices: [$choices]")
        }
    }

    private suspend fun prepareChoices(word: String): Array<String> {
        val translation = words.translation(word)
        val choices = getChoices(translation)
        val idx = choices.indexOf(translation)

        quiz.set(word, choices, if (idx != -1) idx else CHOICE_COUNT, translation)
        return choices
    }

    private suspend fun prepareChoicesV2(wordKey: String): Array<String> {
        val translation = words.translationV2(wordKey)
        val (choices, answer) = getChoicesV2(Languages.getDstLang(wordKey), translation)

        quiz.setV2(wordKey, choices, answer, translation)
        return choices
    }

    private suspend fun getChoices(translation: String): Array<String> {
        val choices = mutableSetOf<String>()
        if (Random.nextInt(2) == 0) {
            choices.add(translation)
        }
        while (choices.size < CHOICE_COUNT) {
            choices.add(words.random().translation)
        }
        return choices.toList().shuffled().toTypedArray()
    }

    // TODO [bug]: need to get only same dstLang choices
    //             cannot do now because dstLang count >= 4 is not guaranteed.
    private suspend fun getChoicesV2(dstLang: Languages, translation: String): Pair<Array<String>, Int> {
        val choices = mutableSetOf<String>()
        if (Random.nextInt(2) == 0) {
            choices.add(translation)
        }
        while (choices.size < CHOICE_COUNT) {
            choices.add(words.randomV2(null).translation)
        }
        val shuffledChoices = choices.toList().shuffled().toTypedArray()
        val idx = shuffledChoices.indexOf(translation)
        val answer = if (idx != -1) idx else CHOICE_COUNT
        return Pair(shuffledChoices, answer)
    }

    // TODO [bug]: Do proper mutex. This can receive multiple "answerQuiz" when it's clicked very fast.
    //             Maybe same countermeasure for CSRF (create token for each quiz)
    private suspend fun handleQuizAnswer(request: dynamic, response: dynamic) {
        quizQueue.dequeue()
        val quizWord = quiz.get(request.word)
        val result = quizWord.answer == request.guess

        val res = if (quizWord.answer != CHOICE_COUNT) {
            createProps("result", result, "answer", quizWord.answer)
        } else {
            createProps("result", result, "answer", quizWord.answer, "translation", quizWord.translation)
        }
        response(res)

        badge.update()
        words.addQuizResult(request.word, result)
        alarms.create(request.word)
        quiz.clear()
    }

    private suspend fun handleQuizAnswerV2(request: dynamic, response: dynamic) {
        quizQueue.dequeue()
        val quizWord = quiz.getV2()
        val correct = quizWord.answer == request.guess

        val res = if (quizWord.answer != CHOICE_COUNT) {
            createProps("correct", correct, "answer", quizWord.answer)
        } else {
            createProps("correct", correct, "answer", quizWord.answer, "translation", quizWord.translation)
        }
        response(res)

        badge.update()
        if (correct) words.incrementCorrectCountV2(request.wordKey)
        alarms.createV2(request.wordKey)
        quiz.clear()
    }

    private suspend fun getAllData(response: dynamic) {
        val words = words.getWordsAsArray()
        val quizQueue = quizQueue.getQuizQueue()
        val alarms = alarms.getAll()

        response(createProps("words", words, "quizQueue", quizQueue, "alarms", alarms))
    }

    private suspend fun getAllDataV2(response: dynamic) {
        val words = words.getWordsAsArrayV2(null)
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
