package storage

import Languages
import Util.Companion.printlnWithTime

class StorageV2 : Storage() {

    suspend fun getVersion() = getStorage("storageVersion", 1) as Int
    suspend fun setVersion(version: Int) = setStorage("storageVersion", version)

    suspend fun getWords(src: Languages, dst: Languages) = getStorage("words-${src.key}${dst.key}", js("{}"))
    suspend fun setWords(src: Languages, dst: Languages, words: dynamic) {
//        setStorage("words-${src.key}${dst.key}", words)
        printlnWithTime("words-${Languages.getLangKey(src, dst)}")
        printlnWithTime(JSON.stringify(words))
    }

    suspend fun getQuizQueue(): Array<dynamic> = getStorage("quizQueue", arrayOf<dynamic>())
    suspend fun setQuizQueue(queue: Array<dynamic>) {
//        setStorage("quizQueue", queue)
        printlnWithTime(JSON.stringify(queue))
    }
}
