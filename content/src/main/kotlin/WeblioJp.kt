import Util.Companion.createProps
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLTableCellElement
import kotlin.browser.document

class WeblioJp {

    fun register() {
        if (invalidPage()) return
        chrome.runtime.sendMessage(null, wordRegistrationProps())
    }

    private fun wordRegistrationProps(): dynamic {
        return createProps(
            "msgType", "registerWord",
            "word", getSearchWord(),
            "translation", getTranslation()
        )
    }

    private fun getTranslation(): String {
        return (document.querySelector("#summary td.ej") as HTMLTableCellElement).firstChild!!.textContent!!
    }

    private fun getSearchWord(): String {
        return (document.querySelector("#h1Query") as HTMLSpanElement).innerText
    }

    private fun invalidPage(): Boolean {
        return !isEnglish2Japanese()
    }

    private fun isEnglish2Japanese() = document.querySelector("#summary .ej") != null
}
