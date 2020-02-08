package storage

import Languages
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

    suspend fun addV2(wordKey: String, translation: String): Boolean {
        mutex.withLock {
            val words = getWordsV2(null)
            if (contains(words, wordKey)) {
                printlnWithTime("[$wordKey] ignored registered word")
                return false
            }
            printlnWithTime("[$wordKey] register new word")
            words[wordKey] = createProps("translation", translation, "correctCount", 0)
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
    suspend fun translationV2(wordKey: String) = getWordsV2(null)[wordKey].translation as String

    suspend fun random(): dynamic {
        val words = getWords()
        val keys = keys(words)
        return words[keys[Random.nextInt(keys.size)]]
    }

    suspend fun randomV2(langKey: String?): dynamic {
        val words = getWordsV2(langKey)
        val keys = keys(words)
        return if (keys.size != 0) {
            words[keys[Random.nextInt(keys.size)]]
        } else null
    }

    suspend fun changeTranslation(word: String, translation: String) {
        mutex.withLock {
            val words = getWords()
            words[word].translation = translation
            setWords(words)
        }
    }

    suspend fun changeTranslationV2(wordKey: String, translation: String) {
        mutex.withLock {
            val words = getWordsV2(null)
            words[wordKey].translation = translation
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

    suspend fun incrementCorrectCountV2(wordKey: String) {
        mutex.withLock {
            val words = getWordsV2(null)
            words[wordKey].correctCount += 1
            setWords(words)
        }
    }

    suspend fun quizResult(word: String) = getWords()[word].quizResult as Array<Boolean>
    suspend fun correctCountV2(wordKey: String) = getWordsV2(null)[wordKey].correctCount as Int

    private fun keys(words: dynamic) = js("Object").keys(words) as Array<String>
    suspend fun size() = keys(getWords()).size
    suspend fun sizeV2(langKey: String?) = keys(getWordsV2(langKey)).size

    private fun contains(words: dynamic, word: String) = keys(words).contains(word)

    private suspend fun getWords() = getStorage("words", js("{}"))
    private suspend fun getWordsV2(langKey: String?): dynamic {
        val words = getStorage("words", js("{}"))
        if (langKey == null) {
            return words
        }
        val filteredWords = createProps()
        return keys(words)
            .filter { Languages.getLangKey(it) == langKey }
            .forEach { filteredWords[it] = words[it] }
    }

    suspend fun setWords(words: dynamic) = setStorage("words", words)

    suspend fun getWordsAsArray(): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        return keys(words)
            .map { arrayFormat(it, words[it].translation, words[it].quizResult) }
            .toTypedArray()
    }

    suspend fun getWordsAsArrayV2(langKey: String?): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        return keys(words)
            .filter {
                if (langKey != null) {
                    Languages.getLangKey(it) == langKey
                } else true
            }
            .map { arrayFormatV2(it, words[it].translation, words[it].correctCount) }
            .toTypedArray()
    }

    private fun arrayFormat(word: String, translation: String, quizResult: Array<Boolean>): dynamic {
        return createProps("word", word, "translation", translation, "quizResult", quizResult)
    }

    private fun arrayFormatV2(wordKey: String, translation: String, correctCount: Int): dynamic {
        return createProps("wordKey", wordKey, "translation", translation, "correctCount", correctCount)
    }
}
