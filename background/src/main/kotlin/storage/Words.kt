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
            val words = getWords(null)
            if (contains(words, word)) {
                delete(words[word])
                setWords(words)
                printlnWithTime("[$word] removed from words")
            }
        }
    }

    suspend fun translation(wordKey: String) = getWords(null)[wordKey].translation as String

    suspend fun randomTranslation(dstLang: String): String {
        return getWordsAsArray { wordKey -> Languages.getDstLang(wordKey).key == dstLang }
            .random()
            .translation as String
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
    suspend fun sizeOfDstLang(dstLang: String): Int {
        return getWordsAsArray { wordKey -> Languages.getDstLang(wordKey).key == dstLang }.size
    }

    private fun contains(words: dynamic, word: String) = keys(words).contains(word)

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

    suspend fun getWordsAsArray(filter: (word: dynamic) -> Boolean): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        return keys(words)
            .filter(filter)
            .map { arrayFormat(it, words[it].translation, words[it].correctCount) }
            .toTypedArray()
    }

    private fun arrayFormat(wordKey: String, translation: String, correctCount: Int): dynamic {
        return createProps("wordKey", wordKey, "translation", translation, "correctCount", correctCount)
    }
}
