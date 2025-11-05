package serializationFiles

import TOMLSerializationException
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object TomlSerialization {

    inline fun <reified T : Any> encodeToString(value: T): String {
        return buildTomlString(value)
    }

    inline fun <reified T : Any> decodeFromString(tomlString: String): T {
        val tomlResult = Toml.parse(tomlString)
        if (tomlResult.hasErrors()) {
            throw TOMLSerializationException("Ошибка парсинга объекта TOML: ${tomlResult.errors()}")
        }
        return parseTomlToObject(tomlResult, T::class)
    }

    fun buildTomlString(obj: Any, prefix: String = ""): String {
        val builtString = StringBuilder()
        val kClass = obj::class
        val properties = kClass.memberProperties

        properties.forEach { property ->
            val value = property.getter.call(obj)
            val key = if (prefix.isNotEmpty()) "$prefix.${property.name}" else property.name

            when (value) {
                null -> {}
                is List<*> -> {
                    if (value.isNotEmpty() && value.first() is Any) {
                        value.filterIsInstance<Any>().forEachIndexed { _, item ->
                            builtString.appendLine("[[$key]]")
                            builtString.appendLine(buildTomlString(item, "").prependIndent("  "))
                        }
                    } else {
                        builtString.append("$key = [")
                        builtString.append(value.joinToString(", ") {
                            when (it) {
                                is String -> "\"$it\""
                                else -> it.toString()
                            }
                        })
                        builtString.appendLine("]")
                    }
                }
                is Map<*, *> -> {
                    builtString.appendLine("[$key]")
                    value.forEach { (mapKey, mapValue) ->
                        if (mapValue is Any) {
                            builtString.appendLine("$mapKey = ${formatValue(mapValue)}")
                        }
                    }
                }
                is Any -> {
                    if (value::class.dataClassFieldsCount > 1) {
                        builtString.appendLine("[$key]")
                        builtString.appendLine(buildTomlString(value, "").prependIndent(""))
                    } else {
                        builtString.appendLine("$key = ${formatValue(value)}")
                    }
                }
            }
        }
        return builtString.toString()
    }

    fun <T : Any> parseTomlToObject(toml: TomlParseResult, kClass: KClass<T>): T {
        val constructor = kClass.primaryConstructor ?: throw IllegalArgumentException("Ошибка отсутствия первичного конструктора у класса: ${kClass.simpleName}")

        val args = mutableMapOf<KParameter, Any?>()
        val parameters = constructor.parameters

        parameters.forEach { parameter ->
            val parameterName = parameter.name ?: throw IllegalArgumentException("Имя параметра не может быть null!")

            val value = when (val parameterType = parameter.type.classifier) {
                String::class -> toml.getString(parameterName) ?: throw TOMLSerializationException("Ошибка отсутствия поля: $parameterName")
                Int::class -> toml.getLong(parameterName)?.toInt() ?: throw TOMLSerializationException("Ошибка отсутствия поля: $parameterName")
                Long::class -> toml.getLong(parameterName) ?: throw TOMLSerializationException("Ошибка отсутствия поля: $parameterName")
                Double::class -> toml.getDouble(parameterName) ?: throw TOMLSerializationException("Ошибка отсутствия поля: $parameterName")
                Float::class -> toml.getDouble(parameterName)?.toFloat() ?: throw TOMLSerializationException("Ошибка отсутствия поля: $parameterName")
                Boolean::class -> toml.getBoolean(parameterName) ?: throw TOMLSerializationException("Ошибка отсутствия поля: $parameterName")
                List::class -> {
                    toml.getArray(parameterName)?.toList()?.map { it } ?: emptyList<String>()
                }
                else -> {
                    if (parameterType is KClass<*> && parameterType.isData) {
                        try {
                            parseTomlToObject(toml, parameterType)
                        } catch (e: Exception) {
                            throw TOMLSerializationException("Ошибка парсинга вложенного объекта: $parameterName: ${e.message}")
                        }
                    } else {
                        throw TOMLSerializationException("Ошибка типа поля: $parameterName: $parameterType")
                    }
                }
            }
            args[parameter] = value
        }

        return constructor.callBy(args)
    }

    private fun formatValue(value: Any): String = when (value) {
        is String -> "\"$value\""
        is Boolean -> value.toString()
        is Number -> value.toString()
        else -> "\"$value\""
    }

    private val KClass<*>.dataClassFieldsCount: Int
        get() = this.primaryConstructor?.parameters?.size ?: 0
}