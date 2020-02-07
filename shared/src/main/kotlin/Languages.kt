enum class Languages(val key: String) {
    ENGLISH("en"),
    JAPANESE("ja");

    companion object {
        private val map = values().associateBy(Languages::key)

        fun getLangKey(src: Languages, dst: Languages) = "${src.key}${dst.key}"
        fun splitWordKey(wordKey: String): Pair<String, String> =
            Pair(wordKey.substringAfter(":"), wordKey.substringBefore(":"))

        fun getWordKey(langKey: String, word: String) = "$langKey:$word"
        fun getLangKey(wordKey: String) = wordKey.substringAfter(":")
        fun getWord(wordKey: String) = wordKey.substringBefore(":")
        fun getSrcLanguage(langKey: String) = map[langKey.substring(0, 2)]
        fun getDstLanguage(langKey: String) = map[langKey.substring(2, 4)]
    }
}
