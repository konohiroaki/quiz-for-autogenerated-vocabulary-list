package storage

import Util.Companion.createProps

class StorageV1 : Storage() {

    suspend fun getWordsAsArray(): Array<dynamic> {
        val words = getStorage("words", js("{}"))
        return keys(words)
            .map { arrayFormat(it, words[it].quizResult, words[it].translation) }
            .toTypedArray()
    }

    suspend fun getQuizQueue() = getStorage("quizQueue", arrayOf<String>()) as Array<String>

    private fun keys(words: dynamic) = js("Object").keys(words) as Array<String>
    private fun arrayFormat(key: String, quizResult: Array<Boolean>, translation: String): dynamic {
        return createProps("word", key, "quizResult", quizResult, "translation", translation)
    }
}
