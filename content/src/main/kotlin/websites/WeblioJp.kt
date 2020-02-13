package websites

import Languages
import Languages.ENGLISH
import Languages.JAPANESE
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLTableCellElement
import kotlin.browser.document

class WeblioJp : TranslationWebsiteRegisterer() {

    override fun isValidPage() = isEnglish2Japanese()
    private fun isEnglish2Japanese() = document.querySelector("#summary .ej") != null

    override fun getLanguage(): Pair<Languages, Languages> {
        return Pair(ENGLISH, JAPANESE)
    }

    override fun getSearchWord(): String {
        return (document.querySelector("#h1Query") as HTMLSpanElement).innerText
    }

    override fun getTranslation(): String {
        return (document.querySelector("#summary td.ej") as HTMLTableCellElement).firstChild!!.textContent!!
    }
}
