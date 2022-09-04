package money.tegro.connector

import java.util.*

val version: String by lazy {
    ClassLoader.getSystemResourceAsStream("project.properties")
        ?.let { Properties().apply { load(it) }.getProperty("project.version").trim() }
        ?: "UNKNOWN"
}
