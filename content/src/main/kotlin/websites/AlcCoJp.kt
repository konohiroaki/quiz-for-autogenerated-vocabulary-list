package websites

import Languages.ENGLISH
import Languages.JAPANESE
import org.w3c.dom.*
import kotlin.browser.document

class AlcCoJp : TranslationWebsiteRegisterer() {

    override fun isValidPage() = isSearchWordPresent() && isEnglish2Japanese()
    private fun isSearchWordPresent() = document.querySelector("#searchWord") != null
    private fun isEnglish2Japanese() =
        (document.querySelector("#f1 > input[name=dk]") as HTMLInputElement).value == "EJ"

    override fun getSrcLanguage() = ENGLISH
    override fun getDstLanguage() = JAPANESE

    override fun getSearchWord() = (document.querySelector("#searchWord") as HTMLSpanElement).innerText

    override fun getTranslation(): String {
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
