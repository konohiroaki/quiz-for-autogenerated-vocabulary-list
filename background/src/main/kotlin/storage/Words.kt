package storage

import Util.Companion.createProps
import Util.Companion.printlnWithTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

private external fun delete(obj: dynamic): Boolean

class Words : Storage() {

    private val mutex = Mutex()

    suspend fun add(word: String, translation: String): Boolean {
        mutex.withLock {
            val words = getWords()
            if (contains(words, word)) {
                printlnWithTime("[${word}] ignored registered word")
                return false
            }
            printlnWithTime("[${word}] register new word")
            words[word] = createProps("translation", translation, "quizResult", arrayOf<Boolean>())
            setWords(words)
            return true
        }
    }

    suspend fun remove(word: String) {
        mutex.withLock {
            val words = getWords()
            if (contains(words, word)) {
                delete(words[word])
                setWords(words)
                printlnWithTime("[$word] removed from words")
            }
        }
    }

    suspend fun translation(word: String) = getWords()[word].translation as String
    suspend fun random(): dynamic {
        val words = getWords()
        val keys = keys(words)
        return words[keys[Random.nextInt(keys.size)]]
    }

    suspend fun changeTranslation(word: String, translation: String) {
        mutex.withLock {
            val words = getWords()
            words[word].translation = translation
            setWords(words)
        }
    }

    suspend fun addQuizResult(word: String, result: Boolean) {
        mutex.withLock {
            val words = getWords()
            printlnWithTime("[$word] quiz result: $result")
            words[word].quizResult = getNewQuizResultArray(words, word, result)
            setWords(words)
        }
    }

    private fun getNewQuizResultArray(words: dynamic, word: String, result: Boolean): Array<Boolean> {
        val list = (words[word].quizResult as Array<Boolean>).toMutableList()
        list.add(result)
        return list.toTypedArray()
    }

    suspend fun quizResult(word: String) = getWords()[word].quizResult as Array<Boolean>

    private fun keys(words: dynamic) = js("Object").keys(words) as Array<String>
    suspend fun size() = keys(getWords()).size

    private fun contains(words: dynamic, word: String) = keys(words).contains(word)

    private suspend fun getWords() = getStorage("words", js("{}"))
    private suspend fun setWords(words: dynamic) = setStorage("words", words)

    suspend fun getWordsAsArray(): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        val keys = keys(words)
        val list = mutableListOf<dynamic>()
        for (key in keys) {
            list.add(
                createProps(
                    "word", key,
                    "quizResult", words[key].quizResult,
                    "translation", words[key].translation
                )
            )
        }
        return list.toTypedArray()
    }
}
