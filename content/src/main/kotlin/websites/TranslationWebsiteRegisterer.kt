package websites

import Languages
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
            "wordKey", "${Languages.getLangKey(getSrcLanguage(), getDstLanguage())}:${getSearchWord()}",
            "translation", getTranslation()
        )
    }

    protected abstract fun isValidPage(): Boolean
    protected abstract fun getSrcLanguage(): Languages
    protected abstract fun getDstLanguage(): Languages
    protected abstract fun getSearchWord(): String
    protected abstract fun getTranslation(): String
}
