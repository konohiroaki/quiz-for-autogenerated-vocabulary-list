package storage

import Util.Companion.createProps
import Util.Companion.printlnWithTime

class Quiz : Storage() {

    suspend fun set(word: String, choices: Array<String>, idx: Int, translation: String) {
        val quiz = createProps(word, createProps("choices", choices, "answer", idx, "translation", translation))
        setStorage("quiz", quiz)
    }

    suspend fun setV2(wordKey: String, choices: Array<String>, idx: Int, translation: String) {
        val quiz = createProps("wordKey", wordKey, "choices", choices, "answer", idx, "translation", translation)
        setStorage("quiz", quiz)
    }

    suspend fun get(word: String): dynamic {
        val quizWord = getStorage("quiz", null)!![word]
        printlnWithTime(JSON.stringify(quizWord))
        return quizWord
    }

    suspend fun getV2(): dynamic {
        val quizWord = getStorage("quiz", null)!!
        printlnWithTime(JSON.stringify(quizWord))
        return quizWord
    }

    suspend fun clear() {
        setStorage("quiz", js("{}"))
    }
}
