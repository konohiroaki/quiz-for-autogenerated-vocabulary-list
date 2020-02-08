import Util.Companion.createProps
import Util.Companion.printlnWithTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import storage.AlarmStorage
import storage.QuizQueue
import storage.Words
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.pow

class Alarm(private val words: Words, private val quizQueue: QuizQueue) {

    private val mutex = Mutex()
    private val storage = AlarmStorage()

    suspend fun create(word: String) {
        mutex.withLock {
            if (!contains(word) && !quizQueue.contains(word)) {
                val quizResult = words.quizResult(word)
                val size = quizResult.filter { it }.size
                val timing = 60 * 2.0.pow(size)

                printlnWithTime("[$word] set alarm: [$timing] minutes")
                chrome.alarms.create(word, createProps("delayInMinutes", timing))

                // for development.
//                printlnWithTime("[$word] set alarm: [${5 * (size + 1)}] seconds")
//                chrome.alarms.create(word, createProps("when", Date.now() + 5000 * (size + 1)))

                storage.dummyUpdate()
            }
        }
    }

    suspend fun createV2(wordKey: String) {
        mutex.withLock {
            if (!contains(wordKey) && !quizQueue.contains(wordKey)) {
                val correctCount = words.correctCountV2(wordKey)
                val timing = 60 * 2.0.pow(correctCount)

                printlnWithTime("[$wordKey] set alarm: [$timing] minutes")
                chrome.alarms.create(wordKey, createProps("delayInMinutes", timing))

                storage.dummyUpdate()
            }
        }
    }

    suspend fun remove(word: String) {
        clear(word)
        storage.dummyUpdate()
    }

    suspend fun contains(word: String) = get(word)?.name == word
    suspend fun get(word: String): dynamic {
        return suspendCoroutine { continuation -> chrome.alarms.get(word) { it -> continuation.resume(it) } }
    }

    suspend fun getAll(): Array<dynamic> {
        return suspendCoroutine { continuation -> chrome.alarms.getAll { it -> continuation.resume(it) } }
    }

    private suspend fun clear(word: String) {
        return suspendCoroutine { continuation -> chrome.alarms.clear(word) { it -> continuation.resume(it) } }
    }
}
