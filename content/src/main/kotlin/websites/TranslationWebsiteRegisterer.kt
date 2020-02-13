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
            "wordKey", "${Languages.getLangKey(getLanguage())}:${getSearchWord()}",
            "translation", getTranslation()
        )
    }

    protected abstract fun isValidPage(): Boolean
    protected abstract fun getLanguage(): Pair<Languages, Languages>
    protected abstract fun getSearchWord(): String
    protected abstract fun getTranslation(): String
}
