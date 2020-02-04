import Util.Companion.printlnWithTime
import storage.Storage

suspend fun main() {
    printlnWithTime("extension loaded")

    val storage = Storage()
    if (storage.getVersion() == "1") {
        upgradeStorageVersion()
    }
    Background()
}

fun upgradeStorageVersion() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
