package money.tegro.catchup

import mu.KLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan("money.tegro.catchup.properties")
@SpringBootApplication
class Application : CommandLineRunner {
    override fun run(vararg args: String?) {
        logger.info("Joining thread, you can press Ctrl+C to shutdown application")
        Thread.currentThread().join()
    }

    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args)
        }
    }
}

