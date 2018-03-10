package io.github.johnfg10

import io.github.johnfg10.command.Command
import io.github.johnfg10.command.CommandArg
import io.github.johnfg10.command.CommandArgumentType
import io.github.johnfg10.command.CommandController
import io.github.johnfg10.command.flags.CommandArgument
import io.github.johnfg10.command.flags.containsFlag
import io.github.johnfg10.roles.Role
import io.github.johnfg10.user.User
import junit.framework.Test
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

@CommandController
class TestCommandHandler{
    @Command("test", ["test"])
    fun test(@CommandArg(CommandArgumentType.Channel) channel: IChannel,
             @CommandArg(CommandArgumentType.Guild) guild: IGuild,
             @CommandArg(CommandArgumentType.Author) author: IUser,
             @CommandArg(CommandArgumentType.Arguments) args: List<CommandArgument>,
             @CommandArg(CommandArgumentType.Mentions) mentions: List<IUser>){
        args.forEach {
            println("${it.flag.flag} : ${it.value}")
        }
    }

    @Command("test", ["testa"])
    @Role("Admin")
    fun permTest(@CommandArg(CommandArgumentType.Channel) channel: IChannel){
        channel.sendMessage("Hiya a!")
    }

    @Command("test", ["testb"])
    @Role("College")
    fun permTestb(@CommandArg(CommandArgumentType.Channel) channel: IChannel){
        channel.sendMessage("Hiya b!")
    }
}