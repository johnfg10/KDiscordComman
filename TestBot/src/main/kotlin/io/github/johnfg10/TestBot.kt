package io.github.johnfg10

import io.github.johnfg10.TestBot.Companion.kDiscord
import io.github.johnfg10.command.Command
import io.github.johnfg10.command.CommandArg
import io.github.johnfg10.command.CommandArgumentType
import io.github.johnfg10.command.flags.CommandArgument
import io.github.johnfg10.command.flags.CommandFlag
import io.github.johnfg10.command.flags.containsFlag
import io.github.johnfg10.command.flags.getValue
import io.github.johnfg10.permission.AUserPermission
import io.github.johnfg10.permission.Permission
import io.github.johnfg10.user.User
import sx.blah.discord.util.DiscordException
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

lateinit var botToken: String

fun main(args: Array<String>) {
    botToken = args[0]
    if (botToken.isEmpty())
        throw IllegalArgumentException("a bot token must be provided")

    val client = createClient(botToken, false)

    kDiscord = KDiscord4J(client, "+")
    kDiscord+TestBot()::class
    kDiscord+TestCommandHandler::class

    client.login()
}

fun createClient(token: String, login: Boolean): IDiscordClient { // Returns a new instance of the Discord client
    val clientBuilder = ClientBuilder() // Creates the ClientBuilder instance
    clientBuilder.withToken(token) // Adds the login info to the builder
    try {
        return if (login) {
            clientBuilder.login() // Creates the client instance and logs the client in
        } else {
            clientBuilder.build() // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
        }
    } catch (e: DiscordException) { // This is thrown if there was a problem building the client
        throw e
    }
}

class TestBot {

    companion object {
        lateinit var kDiscord: KDiscord4J
    }

    @Command("test", ["test"], "testing")
    @Permission("test.testing")
    fun testcmd(@CommandArg(CommandArgumentType.Author) author: IUser,
                @CommandArg(CommandArgumentType.Mentions) users: List<IUser>,
                @CommandArg(CommandArgumentType.Prefix) prefix: String,
                @CommandArg(CommandArgumentType.Message) message: IMessage,
                @CommandArg(CommandArgumentType.Guild) guild: IGuild,
                @CommandArg(CommandArgumentType.Channel) channel: IChannel,
                @CommandArg(CommandArgumentType.Command) command: String,
                @CommandArg(CommandArgumentType.Flags) flags: List<CommandFlag>,
                @CommandArg(CommandArgumentType.Arguments) args: List<CommandArgument>
    ){
        channel.sendMessage(
                "Full test suite: \n" +
                        "author name: ${author.name} \n" +
                        "mentions: ${users.forEach { it.name }} \n" +
                        "prefix: $prefix \n" +
                        "message: ${message.content} \n" +
                        "guild id: ${guild.longID} \n" +
                        "command: $command \n" +
                        "flags: $flags \n" +
                        "args: $args \n"
        )
        if (flags.containsFlag("p"))
            channel.sendMessage("p flag found")

        val value = args.getValue("p")
        if (value != null)
            channel.sendMessage("val : $value")
    }

    @Command("permme", ["permme"], "")
    fun permMe(@CommandArg(CommandArgumentType.Author) author: IUser, @CommandArg(CommandArgumentType.Channel) channel: IChannel, @CommandArg(CommandArgumentType.Guild) guild: IGuild){
        kDiscord.iPermisionStorage.giveUserPermission(User(author.longID, guild.longID), "test.testing")
    }
}