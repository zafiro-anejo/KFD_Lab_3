package enumFiles

enum class SequentialOperations(val value: String) {
    CUSTOM_TOML_SERIALIZATION("${CompositeParts.CUSTOM_TOML_PART.value} ${BaseWords.SERIALIZATION_WORD.value}"),
    CUSTOM_TOML_DESERIALIZATION("${CompositeParts.CUSTOM_TOML_PART.value} ${BaseWords.DESERIALIZATION_WORD.value}"),
    BUILT_IN_TOML_SERIALIZATION("${CompositeParts.BUILT_IN_TOML_PART.value} ${BaseWords.SERIALIZATION_WORD.value}"),
    BUILT_IN_TOML_DESERIALIZATION("${CompositeParts.BUILT_IN_TOML_PART.value} ${BaseWords.DESERIALIZATION_WORD.value}")
}