package storage

import Util.Companion.createProps
import Util.Companion.printlnWithTime

class Quiz : Storage() {

    suspend fun set(word: String, choices: Array<String>, idx: Int) {
        val quiz = createProps(word, createProps("choices", choices, "expected", idx))
        setStorage("quiz", quiz)
    }

    suspend fun get(word: String): dynamic {
        val quizWord = getStorage("quiz", null)!![word]
        printlnWithTime(JSON.stringify(quizWord))
        return quizWord
    }

    suspend fun clear() {
        setStorage("quiz", js("{}"))
    }
}
