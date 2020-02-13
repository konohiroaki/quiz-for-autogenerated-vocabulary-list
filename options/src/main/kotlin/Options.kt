import Util.Companion.createProps

// TODO [performance]: paging? in case when word list has so many words.
// TODO [feature]: search function in case paging is implemented.
// TODO [refactoring]: create data class for storage data.
fun main() {
    setUpdateHandler()
    updatePage()
}

fun setUpdateHandler() {
    chrome.storage.onChanged.addListener { updatePage() }
}

// TODO [performance]: Only update changed part, instead of rewriting everything.
//                     https://developer.chrome.com/extensions/storage#type-StorageChange
fun updatePage() {
    chrome.runtime.sendMessage(null, createProps("msgType", "getAllData")) { response ->
        WordList.update(response.words, response.alarms, response.quizQueue)
        QuizQueueList.update(response.quizQueue)
    }
}
