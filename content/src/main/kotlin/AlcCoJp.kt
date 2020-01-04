import Util.Companion.createProps
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLOListElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document

class AlcCoJp {

    fun register() {
        if (invalidPage()) return
        chrome.runtime.sendMessage(
            null,
            createProps("msgType", "registerWord", "word", getSearchWord(), "translation", getTranslation())
        )
    }

    private fun invalidPage() = !isSearchWordPresent() || !isEnglish2Japanese()

    private fun isSearchWordPresent() = document.querySelector("#searchWord") != null

    private fun isEnglish2Japanese() =
        (document.querySelector("#f1 > input[name=dk]") as HTMLInputElement).value == "EJ"

    private fun getSearchWord() = (document.querySelector("#searchWord") as HTMLSpanElement).innerText

    private fun getTranslation(): String {
        val ol = document.querySelector("#resultsList ul > li ol") as HTMLOListElement
        val li = ol.querySelector("li")
        return if (li == undefined) {
            ol.innerText
        } else {
            (li as HTMLLIElement).innerText
        }.substringBefore("\n")
    }
}
