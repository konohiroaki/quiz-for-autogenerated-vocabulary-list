package storage

import Languages
import Util.Companion.createProps
import Util.Companion.printlnWithTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private external fun delete(obj: dynamic): Boolean

class Words : Storage() {

    private val mutex = Mutex()

    suspend fun add(wordKey: String, translation: String): Boolean {
        mutex.withLock {
            val words = getWords()
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

    suspend fun getTranslation(wordKey: String) = getWords()[wordKey].translation as String

    suspend fun getRandomTranslation(dstLang: String, filterOutTranslation: String): String {
        return getWordsAsArray { wordKey -> Languages.getDstLang(wordKey).key == dstLang }
            .filter { word -> word.translation != filterOutTranslation }
            .random()
            .translation as String
    }

    suspend fun changeTranslation(wordKey: String, translation: String) {
        mutex.withLock {
            val words = getWords()
            words[wordKey].translation = translation
            setWords(words)
        }
    }

    suspend fun incrementCorrectCount(wordKey: String) {
        mutex.withLock {
            val words = getWords()
            words[wordKey].correctCount += 1
            setWords(words)
        }
    }

    suspend fun getCorrectCount(wordKey: String) = getWords()[wordKey].correctCount as Int

    suspend fun getSizeForDstLang(dstLang: String): Int {
        return getWordsAsArray { wordKey -> Languages.getDstLang(wordKey).key == dstLang }.size
    }

    private fun contains(words: dynamic, word: String) = keys(words).contains(word)
    private fun keys(words: dynamic) = js("Object").keys(words) as Array<String>

    private suspend fun getWords(): dynamic = getStorage("words", js("{}"))

    suspend fun setWords(words: dynamic) = setStorage("words", words)

    suspend fun getWordsAsArray(filter: (word: dynamic) -> Boolean): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        return keys(words)
            .filter(filter)
            .map {
                createProps("wordKey", it, "translation", words[it].translation, "correctCount", words[it].correctCount)
            }
            .toTypedArray()
    }
}
