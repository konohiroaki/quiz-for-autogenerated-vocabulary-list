enum class Languages(val key: String, val fifthChoiceText: String) {
    ENGLISH("en", "None of the above"),
    JAPANESE("ja", "上記のどれでもない");

    companion object {
        private val map = values().associateBy(Languages::key)

        fun getLangKey(src: Languages, dst: Languages) = "${src.key}${dst.key}"
        fun getLangKey(language: Pair<Languages, Languages>) = "${language.first.key}${language.second.key}"
        fun splitWordKey(wordKey: String): Pair<String, String> =
            Pair(wordKey.substringAfter(":"), wordKey.substringBefore(":"))

        fun getWordKey(langKey: String, word: String) = "$langKey:$word"
        fun getLangKey(wordKey: String) = wordKey.substringBefore(":")
        fun getWord(wordKey: String) = wordKey.substringAfter(":")
        fun getSrcLang(wordKey: String): Languages = map[wordKey.substring(0, 2)]!!
        fun getDstLang(wordKey: String): Languages = map[wordKey.substring(2, 4)]!!
        fun getLang(key: String?): Languages? = map[key]
    }
}
