package money.tegro.connector

import java.util.*

val version by lazy {
    val props = Properties()
    ClassLoader.getSystemResourceAsStream("connector.properties")?.let { props.load(it) }
    props.getProperty("version", "UNKNOWN").trim()
}
