package storage

import Util.Companion.createProps
import chrome
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class Storage {

    suspend fun getVersion(): String {
        return getStorage("storage_version", "1") as String
    }

    protected suspend fun getStorage(key: String, defaultObj: dynamic): dynamic {
        val storage = suspendCoroutine<dynamic> { continuation ->
            chrome.storage.sync.get(key) { it -> continuation.resume(it) }
        }
        return if (storage[key] != undefined) storage[key] else defaultObj
    }

    protected suspend fun setStorage(key: String, value: dynamic) {
        val props = createProps(key, value)
        suspendCoroutine<Nothing> { continuation -> chrome.storage.sync.set(props) { it -> continuation.resume(it) } }
    }
}
