package net.minecord.cordexproxy.model.controller.config

class ConfigValue(private val stringValue: String) {
    private val booleanValue: Boolean = listOf("true", "on", "yes", "enabled", "allow", "allowed", "1").contains(stringValue)
    private var intValue: Int = 0

    init {
        try {
            this.intValue = Integer.parseInt(stringValue)
        } catch (e: NumberFormatException) {
            this.intValue = 0
        }

    }

    override fun toString(): String {
        return stringValue
    }

    fun asBoolean(): Boolean {
        return booleanValue
    }

    fun asInt(): Int {
        return intValue
    }
}
