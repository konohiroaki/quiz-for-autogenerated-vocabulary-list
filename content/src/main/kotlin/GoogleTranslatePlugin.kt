import Util.Companion.createProps
import org.w3c.dom.*
import kotlin.browser.document

// TODO [bug]: When plugin reloaded and content_script is old in page, it shows error on sendMessage.
//             If possible, maybe should disable content_script when plugin is reloaded.
class GoogleTranslatePlugin {

    fun addListener() {
        val gtxFrameObserver = MutationObserver(checkIfGtx())
        if (document.body != undefined) {
            gtxFrameObserver.observe(document.body!!, MutationObserverInit(childList = true))
        }
    }

    // GoogleTranslate plugin first shows the frame, then asynchronously gets the translation.
    private fun checkIfGtx(): (Array<MutationRecord>, MutationObserver) -> Unit = { mutationRecords, _ ->
        if (isGtxFrame(mutationRecords)) {
            val gtxInnerFrame = mutationRecords[1].addedNodes[0]!!.childNodes[0] as HTMLDivElement
            val gtxObserver = MutationObserver(getGtx())
            gtxObserver.observe(gtxInnerFrame, MutationObserverInit(childList = true))
        }
    }

    private fun isGtxFrame(records: Array<MutationRecord>): Boolean {
        return if (records.size == 2
            && records[0].type == "childList" && records[0].addedNodes.length == 1
            && records[1].type == "childList" && records[1].addedNodes.length == 1
        ) {
            val addedNode0 = records[0].addedNodes[0]
            val addedNode1 = records[1].addedNodes[0]
            val isGtxAnchor = addedNode0 is HTMLDivElement && addedNode0.getAttribute("id") == "gtx-anchor"
            val isGtxFrame = addedNode1 is HTMLDivElement && addedNode1.getAttribute("class") == "jfk-bubble gtx-bubble"
            isGtxAnchor && isGtxFrame
        } else {
            false
        }
    }

    private fun getGtx(): (Array<MutationRecord>, MutationObserver) -> Unit = { mutationRecords, _ ->
        if (isGtx(mutationRecords)) {
            val gtxHost = mutationRecords[0].addedNodes[0]!! as HTMLDivElement
            val div = gtxHost.shadowRoot!!.childNodes[1] as HTMLDivElement
            val srcLang = (div.querySelector(".gtx-lang-selector > option[selected]") as HTMLOptionElement).innerText
            val dstLang = (div.querySelectorAll(".gtx-language")[1] as HTMLDivElement).innerText
            val gtxBody = div.querySelectorAll(".gtx-body")
            val srcWord = (gtxBody[0] as HTMLElement).innerText
            val dstWord = (gtxBody[1] as HTMLElement).innerText

            if (srcLang == "英語" && dstLang == "日本語" && isNotSentence(srcWord)) {
                chrome.runtime.sendMessage(
                    null,
                    createProps("msgType", "registerWord", "word", srcWord, "translation", dstWord)
                )
            }
        }
    }

    private fun isGtx(records: Array<MutationRecord>): Boolean {
        return records.size == 1 && records[0].type == "childList" && records[0].addedNodes.length == 1
    }

    // TODO [bug]: This avoids idioms. Not sure how to accept idioms but not sentences.
    private fun isNotSentence(word: String): Boolean = !word.contains(" ")
}
