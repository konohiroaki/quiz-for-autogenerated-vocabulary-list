import Util.Companion.printlnWithTime
import storage.StorageConverter

suspend fun main() {
    printlnWithTime("extension loaded")

    StorageConverter.convertIfNecessary()

    Background()
}
