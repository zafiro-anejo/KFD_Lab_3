package dataClassesFiles

data class BenchmarkResult(
    val operationName: String,
    val dataSize: Int,
    val timeMs: Long
)