package eu.vironlab.mc.extension

data class BooleanConfiguration(val value: Boolean)

fun Boolean.toConfig(): BooleanConfiguration {
    return BooleanConfiguration(this)
}