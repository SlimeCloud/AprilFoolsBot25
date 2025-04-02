package de.slimecloud.aprilfools

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.requests.CompletedRestAction
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.random.Random

class AprilListener(val main: AprilFoolsBot) : ListenerAdapter() {
    private val words = main.config.sentence.words().map { it.lowercase() }

    private lateinit var role: Role

    override fun onReady(event: ReadyEvent) {
        role = event.jda.getRoleById(main.config.role)!!
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild || event.author.isBot) return

        handleKeywords(event)
        handleObfuscation(event)
    }

    fun handleKeywords(event: MessageReceivedEvent) {
        main.config.hints.filter { it.check(event.message.contentRaw) }.forEach {
            scheduleHint(event.message, it.hint)
        }
    }

    fun handleObfuscation(event: MessageReceivedEvent) {
        val content = event.message.contentRaw
        if (content.replace("\\s+".toRegex(), " ").equals(main.config.sentence, ignoreCase = true)) {
            handleFullReveal(event)
            return
        }

        val newContent = content.replace("(?<=\\W|_|^)(?<word>${words.joinToString("|") { Pattern.quote(it) }})(?=\\W|_|$)".toRegex(RegexOption.IGNORE_CASE)) {
            val code = 'α'.code + words.indexOf(it.groups["word"]!!.value.lowercase())
            "**${code.toChar()}**"
        }

        if (newContent == content) return

        event.message.delete().queue()
        getWebhook(event.channel).queue { webhook -> webhook.send(
            WebhookMessageBuilder()
                .setUsername(event.member?.effectiveName)
                .setAvatarUrl(event.member?.effectiveAvatarUrl)
                .setContent(StringUtils.abbreviate(newContent, Message.MAX_CONTENT_LENGTH))
                .build()
        ) }
    }

    private fun scheduleHint(message: Message, hint: String) {
        main.executor.schedule({
            message.reply(hint).mentionRepliedUser(false).queue()
        }, Random.nextInt(main.config.minResponseDelay, main.config.maxResponseDelay).toLong(), TimeUnit.SECONDS)
    }

    private fun handleFullReveal(event: MessageReceivedEvent) {
        event.message.delete().queue()

        if (role in event.member!!.roles) return

        event.channel.sendMessageEmbeds(
            EmbedBuilder()
                .setTitle("April April!")
                .setColor(0x569d3c)
                .setDescription(main.config.revealMessage.replace("<emoji:(?<name>\\w+)>".toRegex()) { event.guild.getEmojisByName(it.groups["name"]!!.value, true)[0].asMention })
                .setThumbnail(event.guild.iconUrl)
                .setAuthor(event.message.member!!.effectiveName, null, event.message.member!!.effectiveAvatarUrl)
                .build()
        ).queue()

        event.guild.addRoleToMember(event.member!!, role).queue()
    }

    private fun getWebhook(channel: MessageChannel): RestAction<WebhookClient> {
        val name = "1. April ${channel.jda.selfUser.idLong}"

        val container = (if (channel is ThreadChannel) channel.parentChannel else channel) as IWebhookContainer
        return container.retrieveWebhooks().flatMap { hooks -> hooks
            .find { it.name == name }
            ?.let { CompletedRestAction(main.jda, it) }
            ?: container.createWebhook(name)
        }.map { JDAWebhookClient.withUrl(it.url) }.map { if (channel is ThreadChannel) it.onThread(channel.idLong) else it }
    }
}