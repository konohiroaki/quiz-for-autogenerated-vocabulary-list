import Util.Companion.createProps
import storage.QuizQueue
import storage.Words
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Badge(private val words: Words, private val quizQueue: QuizQueue) {

    // TODO: when wordKey in queue, check if that lang word has 4 or more registration. if present, count.
    suspend fun update() {
        val wordsSize = words.size(null)
        val quizQueueSize = quizQueue.size()
        val badgeText = if (wordsSize >= 4 && quizQueueSize > 0) quizQueueSize.toString() else ""

        set(badgeText)
    }

    private suspend fun set(badgeText: String) {
        suspendCoroutine<dynamic> { continuation ->
            chrome.browserAction.setBadgeText(createProps("text", badgeText)) { it -> continuation.resume(it) }
        }
    }
}
