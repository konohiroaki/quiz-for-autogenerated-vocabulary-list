import org.w3c.dom.NodeList
import org.w3c.dom.asList
import kotlin.browser.document
import kotlin.js.Date

external val chrome: dynamic

class Util {
    companion object {
        fun createProps(vararg keyVal: dynamic): dynamic {
            val props = js("{}")
            for (i in 0 until keyVal.size / 2) {
                props[keyVal[i * 2]] = keyVal[i * 2 + 1]
            }
            return props
        }

        fun printlnWithTime(str: Any?) {
            val time = Date(Date.now()).toLocaleTimeString()
            println("[$time] $str")
        }

        fun <T> select(selector: String): T {
            @Suppress("UNCHECKED_CAST")
            return document.querySelector(selector) as T
        }

        fun <T> selectAll(selector: String): Array<T> {
            val elements: NodeList = document.querySelectorAll(selector)
            val list = mutableListOf<T>()
            for (element in elements.asList()) {
                @Suppress("UNCHECKED_CAST")
                list.add(element as T)
            }
            return list.toTypedArray()
        }

    }
}
