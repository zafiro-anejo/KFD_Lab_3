package enumFiles

enum class CompositeParts(val value: String) {
    CUSTOM_TOML_PART("${BaseWords.CUSTOM_WORD.value} ${BaseWords.TOML_WORD.value}"),
    BUILT_IN_TOML_PART("${BaseWords.BUILT_IN_WORD.value} ${BaseWords.TOML_WORD.value}")
}