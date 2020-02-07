package storage

import Languages
import Languages.ENGLISH
import Languages.JAPANESE
import Util.Companion.createProps

class StorageConverter {

    companion object {
        private val storageV1 = StorageV1()
        private val storageV2 = StorageV2()

        suspend fun convertIfNecessary() {
            if (storageV2.getVersion() == 1) {
                convertV1toV2()
            }
        }

        private suspend fun convertV1toV2() {
            storageV2.setVersion(2)
            convertWordsV1toV2()
            convertQuizQueueV1toV2()
        }

        private suspend fun convertWordsV1toV2() {
            val newWords = createProps()
            storageV1.getWordsAsArray().forEach { oldWord ->
                val correctCount = (oldWord.quizResult as Array<Boolean>).count { it }
                newWords[oldWord.word] = createProps("translation", oldWord.translation, "correctCount", correctCount)
            }
            storageV2.setWords(ENGLISH, JAPANESE, newWords)
        }

        private suspend fun convertQuizQueueV1toV2() {
            val newQuizQueue: Array<dynamic> = storageV1.getQuizQueue().map { element ->
                createProps("language", Languages.getLangKey(ENGLISH, JAPANESE), "word", element)
            }.toTypedArray()
            storageV2.setQuizQueue(newQuizQueue)
        }
    }
}
