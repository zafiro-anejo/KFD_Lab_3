import kotlin.system.measureTimeMillis

class Benchmark {
    companion object {
        fun <T, R> measureOperation(
            operationName: String,
            data: List<T>,
            operation: (List<T>) -> List<R>
        ): BenchmarkResult {
            val timeMs = measureTimeMillis {
                operation(data)
            }
            return BenchmarkResult(operationName, data.size, timeMs)
        }

        fun runBenchmarks() {
            val testSizes = listOf(10_000, 100_000, 1_000_000)
            val results = mutableListOf<BenchmarkResult>()

            for (size in testSizes) {
                val testData = TestDataGenerator.generateTemplates(size)

                // 1. Ваш сериализатор TOML (последовательный)
                results.add(measureOperation(SequentialOperations.CUSTOM_TOML_SERIALIZATION.value, testData) { data ->
                    data.map { TomlSerialization.encodeToString(it) }
                })

                val tomlStrings = testData.map { TomlSerialization.encodeToString(it) }

                results.add(measureOperation(SequentialOperations.CUSTOM_TOML_DESERIALIZATION.value, tomlStrings) { strings ->
                    strings.map { TomlSerialization.decodeFromString<Template>(it) }
                })

                // 2. Встроенный TOML сериализатор (последовательный)
                results.add(measureOperation(SequentialOperations.BUILT_IN_TOML_SERIALIZATION.value, testData) { data ->
                    data.map { BuiltInTomlSerialization.encodeToString(it) }
                })

                val builtInTomlStrings = testData.map { BuiltInTomlSerialization.encodeToString(it) }

                results.add(measureOperation(SequentialOperations.BUILT_IN_TOML_DESERIALIZATION.value, builtInTomlStrings) { strings ->
                    strings.map { BuiltInTomlSerialization.decodeFromString<Template>(it) }
                })

                // 3. Параллельный ваш TOML сериализатор
                results.add(measureOperation(ParallelOperations.PARALLEL_CUSTOM_TOML_SERIALIZATION.value, testData) { data ->
                    ParallelTomlSerialization.encodeToString(data)
                })

                results.add(measureOperation(ParallelOperations.PARALLEL_CUSTOM_TOML_DESERIALIZATION.value, tomlStrings) { strings ->
                    ParallelTomlSerialization.decodeFromString<Template>(strings)
                })

                // 4. Параллельный встроенный TOML сериализатор
                results.add(measureOperation(ParallelOperations.PARALLEL_BUILT_IN_TOML_SERIALIZATION.value, testData) { data ->
                    ParallelBuiltInTomlSerialization.encodeToString(data)
                })

                results.add(measureOperation(ParallelOperations.PARALLEL_BUILT_IN_TOML_DESERIALIZATION.value, builtInTomlStrings) { strings ->
                    ParallelBuiltInTomlSerialization.decodeFromString<Template>(strings)
                })
            }

            printResults(results)
        }

        private fun printResults(results: List<BenchmarkResult>) {
            val grouped = results.groupBy { it.operationName }

            grouped.forEach { (operation, operationResults) ->
                println("$operation:")
                operationResults.sortedBy { it.dataSize }.forEach { result ->
                    val speed = result.dataSize.toDouble() / result.timeMs * 1000
                    println("  ${result.dataSize} записей: ${"%.2f".format(speed)} ед/сек")
                }
                println()
            }

            analyzeResults(results)
        }

        private fun analyzeResults(results: List<BenchmarkResult>) {
            val latestResults = results.filter { it.dataSize == 1_000_000 }

            val customSerialization = latestResults.find { it.operationName == SequentialOperations.CUSTOM_TOML_SERIALIZATION.value }
            val builtInSerialization = latestResults.find { it.operationName == SequentialOperations.BUILT_IN_TOML_SERIALIZATION.value }
            val parallelCustomSerialization = latestResults.find { it.operationName == ParallelOperations.PARALLEL_CUSTOM_TOML_SERIALIZATION.value }
            val parallelBuiltInSerialization = latestResults.find { it.operationName == ParallelOperations.PARALLEL_BUILT_IN_TOML_SERIALIZATION.value }

            val customDeserialization = latestResults.find { it.operationName == SequentialOperations.CUSTOM_TOML_DESERIALIZATION.value }
            val builtInDeserialization = latestResults.find { it.operationName == SequentialOperations.BUILT_IN_TOML_DESERIALIZATION.value }
            val parallelCustomDeserialization = latestResults.find { it.operationName == ParallelOperations.PARALLEL_CUSTOM_TOML_DESERIALIZATION.value }
            val parallelBuiltInDeserialization = latestResults.find { it.operationName == ParallelOperations.PARALLEL_BUILT_IN_TOML_DESERIALIZATION.value }

            // Добавляем вывод отношений производительности
            println("АНАЛИЗ ПРОИЗВОДИТЕЛЬНОСТИ КАСТОМНОГО И ВСТРОЕННОГО СЕРИАЛИЗАТОРОВ:")
            printPerformanceRatios(customSerialization, builtInSerialization, customDeserialization, builtInDeserialization,
                parallelCustomSerialization, parallelBuiltInSerialization, parallelCustomDeserialization, parallelBuiltInDeserialization)
        }

        private fun printPerformanceRatios(
            customSerialization: BenchmarkResult?, builtInSerialization: BenchmarkResult?,
            customDeserialization: BenchmarkResult?, builtInDeserialization: BenchmarkResult?,
            parallelCustomSerialization: BenchmarkResult?, parallelBuiltInSerialization: BenchmarkResult?,
            parallelCustomDeserialization: BenchmarkResult?, parallelBuiltInDeserialization: BenchmarkResult?
        ) {
            // Последовательная сериализация
            if (customSerialization != null && builtInSerialization != null) {
                val customSpeed = customSerialization.dataSize.toDouble() / customSerialization.timeMs * 1000
                val builtInSpeed = builtInSerialization.dataSize.toDouble() / builtInSerialization.timeMs * 1000
                val ratio = if (builtInSpeed > 0) customSpeed / builtInSpeed else 0.0
                println("  Последовательная сериализация: Кастомная/Встроенная = ${"%.2f".format(ratio)}")
            }

            // Последовательная десериализация
            if (customDeserialization != null && builtInDeserialization != null) {
                val customSpeed = customDeserialization.dataSize.toDouble() / customDeserialization.timeMs * 1000
                val builtInSpeed = builtInDeserialization.dataSize.toDouble() / builtInDeserialization.timeMs * 1000
                val ratio = if (builtInSpeed > 0) customSpeed / builtInSpeed else 0.0
                println("  Последовательная десериализация: Кастомная/Встроенная = ${"%.2f".format(ratio)}")
            }

            // Параллельная сериализация
            if (parallelCustomSerialization != null && parallelBuiltInSerialization != null) {
                val customSpeed = parallelCustomSerialization.dataSize.toDouble() / parallelCustomSerialization.timeMs * 1000
                val builtInSpeed = parallelBuiltInSerialization.dataSize.toDouble() / parallelBuiltInSerialization.timeMs * 1000
                val ratio = if (builtInSpeed > 0) customSpeed / builtInSpeed else 0.0
                println("  Параллельная сериализация: Кастомная/Встроенная = ${"%.2f".format(ratio)}")
            }

            // Параллельная десериализация
            if (parallelCustomDeserialization != null && parallelBuiltInDeserialization != null) {
                val customSpeed = parallelCustomDeserialization.dataSize.toDouble() / parallelCustomDeserialization.timeMs * 1000
                val builtInSpeed = parallelBuiltInDeserialization.dataSize.toDouble() / parallelBuiltInDeserialization.timeMs * 1000
                val ratio = if (builtInSpeed > 0) customSpeed / builtInSpeed else 0.0
                println("  Параллельная десериализация: Кастомная/Встроенная = ${"%.2f".format(ratio)}")
            }
        }
    }
}
