package storage

import Languages
import Languages.ENGLISH
import Languages.JAPANESE
import Util.Companion.createProps
import Util.Companion.printlnWithTime

class StorageConverter {

    companion object {
        private val storage = Storage()
        private val storageV1 = StorageV1()
        private val words = Words()
        private val quizQueue = QuizQueue()

        suspend fun convertIfNecessary() {
            if (storage.getVersion() == 1) {
                printlnWithTime("will convert")
                convertV1toV2()
                printlnWithTime("converted")
            }
        }

        private suspend fun convertV1toV2() {
            convertWordsV1toV2()
            convertQuizQueueV1toV2()
            storage.setVersion(2)
        }

        private suspend fun convertWordsV1toV2() {
            val newWords = createProps()
            storageV1.getWordsAsArray().forEach { oldWord ->
                val wordKey = Languages.getWordKey(Languages.getLangKey(ENGLISH, JAPANESE), oldWord.word)
                val correctCount = (oldWord.quizResult as Array<Boolean>).count { it }
                newWords[wordKey] = createProps("translation", oldWord.translation, "correctCount", correctCount)
            }
            words.setWords(newWords)
        }

        private suspend fun convertQuizQueueV1toV2() {
            val newQuizQueue: Array<String> = storageV1.getQuizQueue()
                .map { "${Languages.getLangKey(ENGLISH, JAPANESE)}:$it" }
                .toTypedArray()
            quizQueue.setQuizQueue(newQuizQueue)
        }
    }
}
