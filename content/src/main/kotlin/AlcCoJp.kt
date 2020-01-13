import Util.Companion.createProps
import org.w3c.dom.*
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
        val div = document.querySelector("#resultsList > ul > li > div") as HTMLDivElement
        return if (div.firstChild!!.nodeName == "#text") { // no wordclass
            div.innerText
        } else if (div.querySelector("ol > li") == null) { // no list of translation for wordclass
            (div.querySelector("ol") as HTMLOListElement).innerText
        } else {
            (div.querySelector("ol > li") as HTMLLIElement).innerText
        }.substringBefore("\n").substringBefore("◆")
    }
}
