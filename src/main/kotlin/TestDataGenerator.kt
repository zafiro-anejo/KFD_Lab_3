import dataClassesFiles.Template

object TestDataGenerator {
    private val random = kotlin.random.Random

    fun generateTemplates(count: Int): List<Template> {
        return (1..count).map { _ ->
            Template(
                firstField = random.nextLong(),
                secondField = generateRandomString(),
                thirdField = random.nextInt(),
                fourthField = random.nextBoolean(),
                fifthField = random.nextDouble()
            )
        }
    }

    private fun generateRandomString(): String {
        val charRange = ('a'..'z')

        val randomString = (1..10)
            .map { charRange.random(random) }
            .joinToString("")

        return randomString
    }
}
