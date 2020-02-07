package storage

import Languages
import Util.Companion.createProps
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

    suspend fun enqueueV2(wordKey: String) {
        // mutex to avoid get1 -> edit1 -> get2 -> edit2 -> set1 -> set2
        // force to       get1 -> edit1 -> set1 -> get2 -> edit2 -> set2
        // so quizQueue won't corrupt
        mutex.withLock {
            val queue = getQuizQueueV2().toMutableList()
            queue.add(createProps("language", Languages.getLangKey(wordKey), "word", Languages.getWord(wordKey)))
            setQuizQueue(queue.toTypedArray())
        }
    }

    suspend fun dequeue() = mutex.withLock { setQuizQueue(getQuizQueue().toMutableList().drop(1).toTypedArray()) }
    suspend fun dequeueV2() = mutex.withLock { setQuizQueue(getQuizQueueV2().toMutableList().drop(1).toTypedArray()) }

    suspend fun peek(): String? {
        val queue = getQuizQueue()
        return if (queue.isNotEmpty()) queue[0] else null
    }

    suspend fun peekV2(): String? {
        val queue = getQuizQueueV2()
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

    suspend fun removeV2(wordKey: String) {
        mutex.withLock {
            val (langKey, word) = Languages.splitWordKey(wordKey)
            val queue = getQuizQueueV2()
            val removedArray = queue.filter { it.language != langKey || it.word != word }.toTypedArray()
            setQuizQueue(removedArray)
            if (queue.size != removedArray.size) printlnWithTime("[$langKey:$word] removed from quizQueue")
        }
    }

    suspend fun contains(word: String) = getQuizQueue().contains(word)
    suspend fun containsV2(wordKey: String): Boolean {
        val (langKey, word) = Languages.splitWordKey(wordKey)
        return getQuizQueueV2().firstOrNull { it.language == langKey && it.word == word } != null
    }

    suspend fun size() = getQuizQueue().size
    suspend fun sizeV2() = getQuizQueueV2().size
    suspend fun toJsonString() = JSON.stringify(getQuizQueue())
    suspend fun toJsonStringV2() = JSON.stringify(getQuizQueueV2())

    suspend fun isEmpty() = getQuizQueue().isEmpty()
    suspend fun isEmptyV2() = getQuizQueueV2().isEmpty()

    suspend fun getQuizQueue(): Array<String> = getStorage("quizQueue", arrayOf<String>())
    suspend fun getQuizQueueV2(): Array<dynamic> = getStorage("quizQueue", arrayOf<dynamic>())
    private suspend fun setQuizQueue(value: dynamic) = setStorage("quizQueue", value)
}
