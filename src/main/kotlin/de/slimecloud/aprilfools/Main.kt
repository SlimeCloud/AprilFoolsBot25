package de.slimecloud.aprilfools

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

fun main() {
    val config = Config.readFromFile("config.yaml")
    val credentials = dotenv {
        filename = "credentials"
        ignoreIfMissing = true
    }

    AprilFoolsBot(config, credentials)
}

fun String.words() = split("\\s+".toRegex())

class AprilFoolsBot(
    val config: Config,
    credentials: Dotenv
) {
    val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(0)

    val jda = JDABuilder.createDefault(credentials["DISCORD_TOKEN"] ?: error("DISCORD_TOKEN required"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .addEventListeners(AprilListener(this))
        .build()
}