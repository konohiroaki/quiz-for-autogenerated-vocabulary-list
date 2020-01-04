import Util.Companion.createProps
import storage.QuizQueue
import storage.Words
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Badge(private val words: Words, private val quizQueue: QuizQueue) {

    suspend fun update() {
        val wordsSize = words.size()
        val quizQueueSize = quizQueue.size()
        val badgeText = if (wordsSize >= 4 && quizQueueSize > 0) quizQueueSize.toString() else ""

        set(badgeText)
    }

    private suspend fun set(text: String) {
        suspendCoroutine<dynamic> { continuation ->
            chrome.browserAction.setBadgeText(createProps("text", text)) { it -> continuation.resume(it) }
        }
    }
}
