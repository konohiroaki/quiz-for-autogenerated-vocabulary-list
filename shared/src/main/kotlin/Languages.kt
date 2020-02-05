enum class Languages(val key: String) {
    ENGLISH("en"),
    JAPANESE("ja");

    companion object {
        fun getKey(src: Languages, dst: Languages) = "${src.key}${dst.key}"
    }
}
