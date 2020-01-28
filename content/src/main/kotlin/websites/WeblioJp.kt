package websites

import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLTableCellElement
import kotlin.browser.document

class WeblioJp : TranslationWebsiteRegisterer() {

    override fun getTranslation(): String {
        return (document.querySelector("#summary td.ej") as HTMLTableCellElement).firstChild!!.textContent!!
    }

    override fun getSearchWord(): String {
        return (document.querySelector("#h1Query") as HTMLSpanElement).innerText
    }

    override fun isValidPage(): Boolean {
        return isEnglish2Japanese()
    }

    private fun isEnglish2Japanese() = document.querySelector("#summary .ej") != null
}
