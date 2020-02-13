package websites

import Languages
import Languages.ENGLISH
import Languages.JAPANESE
import org.w3c.dom.*
import kotlin.browser.document

class AlcCoJp : TranslationWebsiteRegisterer() {

    override fun isValidPage(): Boolean {
        val isTranslatedForSearchWord = getSearchWord() == getTranslationWord()
        val translation = getTranslation()
        val isGuidingToOtherPage = translation.startsWith("→") || translation.startsWith("＝")

        return isSearchWordPresent()
                && (isEnglish2Japanese() || isJapanese2English())
                && isTranslatedForSearchWord
                && !isGuidingToOtherPage
    }

    private fun isSearchWordPresent() = document.querySelector("#searchWord") != null
    private fun isEnglish2Japanese() =
        (document.querySelector("#f1 > input[name=dk]") as HTMLInputElement).value == "EJ"

    private fun isJapanese2English() =
        (document.querySelector("#f1 > input[name=dk]") as HTMLInputElement).value == "JE"

    override fun getLanguage(): Pair<Languages, Languages> {
        return if (isEnglish2Japanese()) Pair(ENGLISH, JAPANESE) else Pair(JAPANESE, ENGLISH)
    }

    override fun getSearchWord() = (document.querySelector("#searchWord") as HTMLSpanElement).innerText
    private fun getTranslationWord() =
        (getFirstTranslationElement().querySelector("h2") as HTMLHeadingElement).innerText

    private fun getFirstTranslationElement() = document.querySelector("#resultsList > ul > li") as HTMLLIElement

    override fun getTranslation(): String {
        val div = getFirstTranslationElement().querySelector(":scope > div") as HTMLDivElement

        return when {
            div.firstChild!!.nodeName == "#text" -> div.innerText
            div.querySelector("li") != null -> (div.querySelector("li") as HTMLLIElement).innerText
            div.querySelector("ol > li") != null -> (div.querySelector("ol > li") as HTMLLIElement).innerText
            else -> (div.querySelector("ol") as HTMLOListElement).innerText
        }.substringBefore("\n").substringBefore("◆")
    }
}
