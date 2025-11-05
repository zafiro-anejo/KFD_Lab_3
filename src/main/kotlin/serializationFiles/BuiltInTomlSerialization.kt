package serializationFiles

import dataClassesFiles.Template
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
            else -> throw IllegalArgumentException("Неподдерживаемый тип: ${T::class}")
        }
    }

    inline fun <reified T : Any> decodeFromString(tomlString: String): T {
        val toml = Toml.parse(tomlString)

        return when (T::class) {
            Template::class -> Template(
                firstField = toml.getLong("firstField")
                    ?: throw IllegalArgumentException("Отсутствует поле firstField"),
                secondField = toml.getString("secondField")
                    ?: throw IllegalArgumentException("Отсутствует поле secondField"),
                thirdField = toml.getLong("thirdField")?.toInt()
                    ?: throw IllegalArgumentException("Отсутствует поле thirdField"),
                fourthField = toml.getBoolean("fourthField")
                    ?: throw IllegalArgumentException("Отсутствует поле fourthField"),
                fifthField = toml.getDouble("fifthField")
                    ?: throw IllegalArgumentException("Отсутствует поле fifthField")
            ) as T
            else -> throw IllegalArgumentException("Неподдерживаемый тип: ${T::class}")
        }
    }
}