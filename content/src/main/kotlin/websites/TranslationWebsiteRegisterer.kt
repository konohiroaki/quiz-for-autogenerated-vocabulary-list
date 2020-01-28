package websites

import Util.Companion.createProps
import chrome

abstract class TranslationWebsiteRegisterer {

    fun register() {
        if (isValidPage()) {
            chrome.runtime.sendMessage(null, wordRegistrationProps())
        }
    }

    private fun wordRegistrationProps(): dynamic {
        return createProps(
            "msgType", "registerWord",
            "word", getSearchWord(),
            "translation", getTranslation()
        )
    }

    protected abstract fun isValidPage(): Boolean
    protected abstract fun getSearchWord(): String
    protected abstract fun getTranslation(): String
}
