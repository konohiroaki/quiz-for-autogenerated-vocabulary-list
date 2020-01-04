package storage

import Util.Companion.createProps
import kotlin.random.Random

class AlarmStorage : Storage() {

    // used for firing storage.onChanged for updating alarm info in option page.
    suspend fun dummyUpdate() = setStorage("alarm", createProps("dummy", Random.nextInt()))
}
