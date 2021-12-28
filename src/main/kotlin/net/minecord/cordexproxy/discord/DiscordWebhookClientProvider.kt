package net.minecord.cordexproxy.discord

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder

class DiscordWebhookClientProvider(private val reportWebhook: String, private val urgentWebhook: String) {
    val reportWebhookClient by lazy { provideClient(reportWebhook) }
    val urgentWebhookClient by lazy { provideClient(urgentWebhook) }

    private fun provideClient(webhook: String): WebhookClient {
        val builder = WebhookClientBuilder(webhook)

        builder.setThreadFactory { job: Runnable? ->
            val thread = Thread(job)
            thread.name = webhook
            thread.isDaemon = true
            thread
        }
        builder.setWait(true)

        return builder.build()
    }
}
