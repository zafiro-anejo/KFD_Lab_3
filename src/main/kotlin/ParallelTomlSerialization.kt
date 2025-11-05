import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ParallelTomlSerialization {
    val executor: ExecutorService = Executors.newWorkStealingPool()

    inline fun <reified T : Any> encodeToString(values: List<T>): List<String> {
        if (values.size < 1000) {
            return values.map { TomlSerialization.encodeToString(it) }
        }

        val futures = values.map { value ->
            CompletableFuture.supplyAsync(
                { TomlSerialization.encodeToString(value) },
                executor
            )
        }

        CompletableFuture.allOf(*futures.toTypedArray()).join()
        return futures.map { it.getNow("") }
    }

    inline fun <reified T : Any> decodeFromString(tomlStrings: List<String>): List<T> {
        if (tomlStrings.size < 1000) {
            return tomlStrings.map { TomlSerialization.decodeFromString<T>(it) }
        }

        val futures = tomlStrings.map { tomlString ->
            CompletableFuture.supplyAsync(
                { TomlSerialization.decodeFromString<T>(tomlString) },
                executor
            )
        }

        CompletableFuture.allOf(*futures.toTypedArray()).join()
        return futures.map { it.getNow(null) ?: throw IllegalStateException("TOML decoding failed") }
    }
}
