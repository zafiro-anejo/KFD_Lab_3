enum class ParallelOperations(val value: String) {
    PARALLEL_CUSTOM_TOML_SERIALIZATION("${BaseWords.PARALLEL_WORD.value} ${SequentialOperations.CUSTOM_TOML_SERIALIZATION.value}"),
    PARALLEL_CUSTOM_TOML_DESERIALIZATION("${BaseWords.PARALLEL_WORD.value} ${SequentialOperations.CUSTOM_TOML_DESERIALIZATION.value}"),
    PARALLEL_BUILT_IN_TOML_SERIALIZATION("${BaseWords.PARALLEL_WORD.value} ${SequentialOperations.BUILT_IN_TOML_SERIALIZATION.value}"),
    PARALLEL_BUILT_IN_TOML_DESERIALIZATION("${BaseWords.PARALLEL_WORD.value} ${SequentialOperations.BUILT_IN_TOML_DESERIALIZATION.value}")
}
