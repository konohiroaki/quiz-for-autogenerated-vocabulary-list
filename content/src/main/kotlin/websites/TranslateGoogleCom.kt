package websites

import Languages
import Util.Companion.createProps
import chrome
import org.w3c.dom.*
import kotlin.browser.document

class TranslateGoogleCom : TranslationWebsiteRegisterer() {

    init {
        registerMutationHandler()
    }

    private fun registerMutationHandler() {
        val observer = MutationObserver(mutationHandler())
        observer.observe(document.querySelector(".results-container")!!, MutationObserverInit(true))
    }

    private fun mutationHandler(): (Array<MutationRecord>, MutationObserver) -> Unit = { mutationRecords, _ ->
        for (mutationRecord in mutationRecords) {
            if (mutationRecord.addedNodes.asList().isNotEmpty()) {
                register(mutationRecord)
            }
        }
    }

    private fun register(mutationRecord: MutationRecord) {
        val addedNode = mutationRecord.addedNodes.asList()[0]

        if (isValidPage()) {
            chrome.runtime.sendMessage(
                null, createProps(
                    "msgType", "registerWord",
                    "wordKey", "${Languages.getLangKey(getLanguage())}:${getSearchWord()}",
                    "translation", getTranslation(addedNode.parentElement!!)
                )
            )
        }
    }

    private fun getLangKey(): Pair<String?, String?> {
        val checked = document.querySelectorAll(".jfk-button-checked")
        val srcLangKey = (checked[0] as HTMLDivElement).getAttribute("value")
        val dstLangKey = (checked[1] as HTMLDivElement).getAttribute("value")
        return Pair(srcLangKey, dstLangKey)
    }

    private fun isValidLanguage(langKey: Pair<String?, String?>): Boolean {
        return Languages.getLang(langKey.first) != null && Languages.getLang(langKey.second) != null
    }

    private fun isVerifiedTranslation(): Boolean {
        val verifiedButton = document.querySelector(".trans-verified-button")
        return verifiedButton != null && verifiedButton.getAttribute("style") != "display:none"
    }

    override fun isValidPage(): Boolean {
        return isVerifiedTranslation() && isValidLanguage(getLangKey())
    }

    override fun getLanguage(): Pair<Languages, Languages> {
        val langKey = getLangKey()
        return Pair(Languages.getLang(langKey.first)!!, Languages.getLang(langKey.second)!!)
    }

    override fun getSearchWord(): String {
        return (document.querySelector(".text-dummy") as HTMLDivElement).innerHTML
    }

    override fun getTranslation(): String {
        return getTranslation(document)
    }

    private fun getTranslation(node: ParentNode): String {
        return (node.querySelector(".translation") as HTMLSpanElement).innerText
    }
}
