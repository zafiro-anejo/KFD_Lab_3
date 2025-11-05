import org.tomlj.Toml

object BuiltInTomlSerialization {
    inline fun <reified T : Any> encodeToString(value: T): String {
        return when (value) {
            is Template -> """
                firstField = ${value.firstField}
                secondField = "${value.secondField}"
                thirdField = ${value.thirdField}
                fourthField = ${value.fourthField}
                fifthField = ${value.fifthField}
            """.trimIndent()
            else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
        }
    }

    inline fun <reified T : Any> decodeFromString(tomlString: String): T {
        val toml = Toml.parse(tomlString)

        return when (T::class) {
            Template::class -> Template(
                firstField = toml.getLong("firstField") ?: throw IllegalArgumentException("Missing firstField"),
                secondField = toml.getString("secondField") ?: throw IllegalArgumentException("Missing secondField"),
                thirdField = toml.getLong("thirdField")?.toInt() ?: throw IllegalArgumentException("Missing thirdField"),
                fourthField = toml.getBoolean("fourthField") ?: throw IllegalArgumentException("Missing fourthField"),
                fifthField = toml.getDouble("fifthField") ?: throw IllegalArgumentException("Missing fifthField")
            ) as T
            else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
        }
    }
}
