package storage

import Util.Companion.printlnWithTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QuizQueue : Storage() {

    private val mutex = Mutex()

    suspend fun enqueue(word: String) {
        // mutex to avoid get1 -> edit1 -> get2 -> edit2 -> set1 -> set2
        // force to       get1 -> edit1 -> set1 -> get2 -> edit2 -> set2
        // so quizQueue won't corrupt
        mutex.withLock {
            val queue = getQuizQueue().toMutableList()
            queue.add(word)
            setQuizQueue(queue.toTypedArray())
        }
    }

    suspend fun dequeue() = mutex.withLock { setQuizQueue(getQuizQueue().toMutableList().drop(1).toTypedArray()) }

    suspend fun peek(): String? {
        val queue = getQuizQueue()
        return if (queue.isNotEmpty()) queue[0] else null
    }

    suspend fun remove(word: String) {
        mutex.withLock {
            val queue = getQuizQueue()
            if (queue.contains(word)) {
                val list = queue.toMutableList()
                list.remove(word)
                setQuizQueue(list.toTypedArray())
                printlnWithTime("[$word] removed from quizQueue")
            }
        }
    }

    suspend fun contains(word: String) = getQuizQueue().contains(word)
    suspend fun size() = getQuizQueue().size
    suspend fun toJsonString() = JSON.stringify(getQuizQueue())

    suspend fun isEmpty() = getQuizQueue().isEmpty()

    suspend fun getQuizQueue(): Array<String> = getStorage("quizQueue", arrayOf<String>())
    private suspend fun setQuizQueue(value: dynamic) = setStorage("quizQueue", value)
}
