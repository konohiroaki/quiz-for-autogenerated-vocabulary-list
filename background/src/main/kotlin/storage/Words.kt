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

    suspend fun add(wordKey: String, translation: String): Boolean {
        mutex.withLock {
            val words = getWords(null)
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

    suspend fun translation(wordKey: String) = getWords(null)[wordKey].translation as String

    suspend fun random(langKey: String?): dynamic {
        val words = getWords(langKey)
        val keys = keys(words)
        return if (keys.size != 0) {
            words[keys[Random.nextInt(keys.size)]]
        } else null
    }

    suspend fun changeTranslation(wordKey: String, translation: String) {
        mutex.withLock {
            val words = getWords(null)
            words[wordKey].translation = translation
            setWords(words)
        }
    }

    suspend fun incrementCorrectCount(wordKey: String) {
        mutex.withLock {
            val words = getWords(null)
            words[wordKey].correctCount += 1
            setWords(words)
        }
    }

    suspend fun correctCount(wordKey: String) = getWords(null)[wordKey].correctCount as Int

    private fun keys(words: dynamic) = js("Object").keys(words) as Array<String>
    suspend fun size(langKey: String?) = keys(getWords(langKey)).size

    private fun contains(words: dynamic, word: String) = keys(words).contains(word)

    private suspend fun getWords() = getStorage("words", js("{}"))
    private suspend fun getWords(langKey: String?): dynamic {
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

    suspend fun getWordsAsArray(langKey: String?): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        return keys(words)
            .filter {
                if (langKey != null) {
                    Languages.getLangKey(it) == langKey
                } else true
            }
            .map { arrayFormat(it, words[it].translation, words[it].correctCount) }
            .toTypedArray()
    }

    private fun arrayFormat(wordKey: String, translation: String, correctCount: Int): dynamic {
        return createProps("wordKey", wordKey, "translation", translation, "correctCount", correctCount)
    }
}
