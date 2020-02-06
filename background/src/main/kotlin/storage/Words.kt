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

    suspend fun addV2(langKey: String, word: String, translation: String): Boolean {
        mutex.withLock {
            val words = getWordsV2(langKey)
            if (contains(words, word)) {
                printlnWithTime("[$langKey:$word] ignored registered word")
                return false
            }
            printlnWithTime("[$langKey:$word] register new word")
            words[word] = createProps("translation", translation, "correctCount", 0)
            setWordsV2(langKey, words)
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

    suspend fun removeV2(langKey: String, word: String) {
        mutex.withLock {
            val words = getWordsV2(langKey)
            if (contains(words, word)) {
                delete(words[word])
                setWordsV2(langKey, words)
                printlnWithTime("[$langKey:$word] removed from words")
            }
        }
    }

    suspend fun translation(word: String) = getWords()[word].translation as String
    suspend fun translationV2(langKey: String, word: String) = getWordsV2(langKey)[word].translation as String
    suspend fun random(): dynamic {
        val words = getWords()
        val keys = keys(words)
        return words[keys[Random.nextInt(keys.size)]]
    }

    suspend fun randomV2(langKey: String): dynamic {
        val words = getWordsV2(langKey)
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

    suspend fun changeTranslationV2(langKey: String, word: String, translation: String) {
        mutex.withLock {
            val words = getWordsV2(langKey)
            words[word].translation = translation
            setWordsV2(langKey, words)
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

    suspend fun incrementCorrectCountV2(langKey: String, word: String) {
        mutex.withLock {
            val words = getWordsV2(langKey)
            words[word].correctCount += 1
            setWordsV2(langKey, words)
        }
    }

    suspend fun quizResult(word: String) = getWords()[word].quizResult as Array<Boolean>
    suspend fun correctCountV2(langKey: String, word: String) = getWordsV2(langKey)[word].correctCount as Int

    private fun keys(words: dynamic) = js("Object").keys(words) as Array<String>
    suspend fun size() = keys(getWords()).size
    suspend fun sizeV2(langKey: String) = keys(getWordsV2(langKey)).size

    private fun contains(words: dynamic, word: String) = keys(words).contains(word)

    private suspend fun getWords() = getStorage("words", js("{}"))
    private suspend fun getWordsV2(langKey: String) = getStorage("words-$langKey", js("{}"))
    private suspend fun setWords(words: dynamic) = setStorage("words", words)
    private suspend fun setWordsV2(langKey: String, words: dynamic) = setStorage("words-$langKey", words)

    suspend fun getWordsAsArray(): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        return keys(words)
            .map { arrayFormat(it, words[it].quizResult, words[it].translation) }
            .toTypedArray()
    }

    suspend fun getWordsAsArrayV2(langKey: String): Array<dynamic> {
        val words = getStorage("words-$langKey", js("{}"))
        return keys(words)
            .map { arrayFormatV2(it, words[it].translation, words[it].correctCount) }
            .toTypedArray()
    }

    private fun arrayFormat(key: String, quizResult: Array<Boolean>, translation: String): dynamic {
        return createProps("word", key, "quizResult", quizResult, "translation", translation)
    }

    private fun arrayFormatV2(key: String, translation: String, correctCount: Int): dynamic {
        return createProps("word", key, "translation", translation, "correctCount", correctCount)
    }
}
