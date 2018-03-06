package io.github.johnfg10

import io.github.johnfg10.command.Command
import io.github.johnfg10.command.CommandArg
import io.github.johnfg10.command.CommandArgumentType
import io.github.johnfg10.command.CommandController
import io.github.johnfg10.command.flags.CommandArgument
import io.github.johnfg10.permission.Permission
import io.github.johnfg10.user.User
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

@CommandController
class TestCommandHandler{

    @Command("setPermission", ["setperm", "setpermission"])
    fun SetPermission(@CommandArg(CommandArgumentType.Channel) channel: IChannel,
                      @CommandArg(CommandArgumentType.Guild) guild: IGuild,
                      @CommandArg(CommandArgumentType.Author) author: IUser,
                      @CommandArg(CommandArgumentType.Arguments) args: List<CommandArgument>,
                      @CommandArg(CommandArgumentType.Mentions) mentions: List<IUser>){
        val userPerms = TestBot.kDiscord.iPermisionStorage.findUserPermission(User(author.longID, guild.longID))
        if (guild.owner == author || userPerms?.permissions?.contains("") == true){
            val arggs = args.filter { it.flag.flag == "perm" }
            val arg = arggs.firstOrNull()
            if (arg == null){
                channel.sendMessage("There must be at least one perm argument")
                return
            }

            for (mention in mentions){
                TestBot.kDiscord.iPermisionStorage.giveUserPermission(User(mention.longID, guild.longID), arg.value)
                channel.sendMessage("Added permission ${arg.value} to ${mention.getDisplayName(guild)}")
            }
        }else{
            channel.sendMessage("You do not have permission to execute this command")
        }
    }

    @Command("removePerm", ["removeperm", "removepermission"])
    @Permission("admin.permission.remove")
    fun RemovePermission(@CommandArg(CommandArgumentType.Channel) channel: IChannel,
                         @CommandArg(CommandArgumentType.Guild) guild: IGuild,
                         @CommandArg(CommandArgumentType.Author) author: IUser,
                         @CommandArg(CommandArgumentType.Arguments) args: List<CommandArgument>,
                         @CommandArg(CommandArgumentType.Mentions) mentions: List<IUser>){

        val arggs = args.filter { it.flag.flag == "perm" }
        val arg = arggs.firstOrNull()
        if (arg == null){
            channel.sendMessage("There must be at least one perm argument")
            return
        }

        for (mention in mentions){
            TestBot.kDiscord.iPermisionStorage.giveUserPermission(User(author.longID, guild.longID), arg.value)
            channel.sendMessage("Removed permission ${arg.value} to ${mention.getDisplayName(guild)}")
        }
    }

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

}