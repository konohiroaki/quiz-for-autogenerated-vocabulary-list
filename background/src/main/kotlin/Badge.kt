import Util.Companion.CHOICE_COUNT
import Util.Companion.createProps
import storage.QuizQueue
import storage.Words
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Badge(private val words: Words, private val quizQueue: QuizQueue) {

    suspend fun update() {
        val availableDstLangList = words.getWordsAsArray { true }
            .groupBy { word -> Languages.getDstLang(word.wordKey) }
            .filter { map -> map.value.size > CHOICE_COUNT }
            .map { map -> map.key.key }
        val availableQuizList = quizQueue.getQuizQueue()
            .filter { wordKey -> availableDstLangList.contains(Languages.getDstLang(wordKey).key) }

        val badgeText = if (availableQuizList.isNotEmpty()) availableQuizList.size.toString() else ""

        set(badgeText)
    }

    private suspend fun set(badgeText: String) {
        suspendCoroutine<dynamic> { continuation ->
            chrome.browserAction.setBadgeText(createProps("text", badgeText)) { it -> continuation.resume(it) }
        }
    }
}
