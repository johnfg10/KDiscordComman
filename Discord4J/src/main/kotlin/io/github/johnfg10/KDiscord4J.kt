package io.github.johnfg10

import io.github.johnfg10.command.CommandArg
import io.github.johnfg10.command.CommandArgumentType
import io.github.johnfg10.command.flags.CommandArgument
import io.github.johnfg10.command.flags.CommandFlag
import io.github.johnfg10.command.flags.CommandPhraser
import io.github.johnfg10.permission.Permission
import io.github.johnfg10.storage.IPermmisionStorage
import io.github.johnfg10.storage.JsonPermissionStore
import io.github.johnfg10.user.User
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import java.io.File
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.*

class KDiscord4J(private val discordClient: IDiscordClient, override val iPermisionStorage: IPermmisionStorage,
                 override val defaultPrefix: String) : KDiscordCommand(iPermisionStorage , defaultPrefix) {

    constructor(discordClient: IDiscordClient, file: File, defaultPrefix: String) : this(discordClient,
            JsonPermissionStore(file), defaultPrefix)

    constructor(discordClient: IDiscordClient, defaultPrefix: String) : this(discordClient, File("permissions.json"),
            defaultPrefix)

    constructor(discordClient: IDiscordClient) : this(discordClient, "+=")

    val IUSER_TYPE = IUser::class.createType()
    val ICHANNEL_TYPE = IChannel::class.createType()
    val IGUILD_TYPE = IGuild::class.createType()
    val IMESSAGE_TYPE = IMessage::class.createType()
    val COMMAND_ARGUMENT = CommandArgument::class.createType()
    val COMMAND_FLAG = CommandFlag::class.createType()
    val STRING_TYPE = String::class.createType()
    val LIST_IUSER_TYPE = MutableList::class.createType(listOf(KTypeProjection(null, IUSER_TYPE)))
    val ARRAY_IUSER_TYPE = Array<IUser>::class.createType(listOf(KTypeProjection(null, IUSER_TYPE)))
    val LIST_COMMAND_FLAG_TYPE = MutableList::class.createType(listOf(KTypeProjection(null, COMMAND_FLAG)))
    val LIST_COMMAND_ARGUMENT_TYPE =
            MutableList::class.createType(listOf(KTypeProjection(null, COMMAND_ARGUMENT)))


    init{
        discordClient.dispatcher.registerListener(this)
    }

    @EventSubscriber
    fun onMessageReceivedEvent(msg: MessageReceivedEvent){
        val message = msg.message.content
        val user = discordUserToUser(msg.author, msg.guild)

        val prefix: String = guildPrefix[user] ?: defaultPrefix

        if (message.startsWith(prefix)){
            val msgSplit = message.split(Regex("\\s"))

            val msgCommand = msgSplit[0].replace(prefix, "")

            val cmdMap = commandMap.filter { it.key.aliases.contains(msgCommand) }
            if (cmdMap.isEmpty())
                return

            val cmdKey = cmdMap.keys.first()
            val cmd = cmdMap[cmdKey]

            if (cmd != null){

                if (permissionMap.contains(cmd)){
                    val perms = permissionMap[cmd] ?: throw IllegalArgumentException("Can not load permissions from permission maps")

                    //println(perms)

                    val userPerm = iPermisionStorage.findUserPermission(user)
                    if (userPerm == null) {
                        msg.channel.sendMessage("Unfortunately it appears you do not have any permissions attached to your username, Required perms: ${perms.toFormattedString()}" )
                        return
                    }

                    if(!userPerm.permissions.containsAll(userPerm.permissions)){
                        //println("debug 1")
                        val remainingRequiredPerms = userPerm.permissions - perms.toStringList()
                        msg.channel.sendMessage("You are missing permissions, permissions missing are: ${remainingRequiredPerms.ToFormattedString()}")
                        return
                    }
                }
                //if we get this far all permissions are correct

                val args = mutableMapOf<KParameter, Any?>()

                args[cmd.instanceParameter!!] = classMap[cmd]!!.createInstance()

                for(it in cmd.valueParameters){
                    val cmdAnnotation = it.findAnnotation<CommandArg>()
                    if (cmdAnnotation != null){
                        when(cmdAnnotation.type){
                            CommandArgumentType.Author -> {
                                if (it.type.isSubtypeOf(IUSER_TYPE)){
                                    args[it] = msg.author
                                }
                            }
                            CommandArgumentType.Mentions -> {
                                if (it.type.isSubtypeOf(LIST_IUSER_TYPE)){
                                    args[it] = msg.message.mentions
                                } else if(it.type.isSubtypeOf(ARRAY_IUSER_TYPE)) {
                                    args[it] = msg.message.mentions.toTypedArray()
                                }
                            }
                            CommandArgumentType.Prefix -> {
                                if (it.type.isSubtypeOf(STRING_TYPE)){
                                    args[it] = prefix
                                }
                            }
                            CommandArgumentType.Message -> {
                                if (it.type.isSubtypeOf(IMESSAGE_TYPE)){
                                    args[it] = msg.message
                                }
                            }
                            CommandArgumentType.Guild -> {
                                if (it.type.isSubtypeOf(IGUILD_TYPE)){
                                    args[it] = msg.guild
                                }
                            }
                            CommandArgumentType.Channel -> {
                                if (it.type.isSubtypeOf(ICHANNEL_TYPE))
                                    args[it] = msg.channel
                            }
                            CommandArgumentType.Command -> {
                                if (it.type.isSubtypeOf(STRING_TYPE)){
                                    args[it] = msgCommand
                                }
                            }
                            CommandArgumentType.Arguments -> {
                                if (it.type.isSubtypeOf(LIST_COMMAND_ARGUMENT_TYPE)){
                                    val list = msgSplit.toMutableList()
                                    list.remove(msgSplit[0])
                                    args[it] = CommandPhraser.PhraseArguments(list)
                                }
                            }
                            CommandArgumentType.Flags -> {
                                if (it.type.isSubtypeOf(LIST_COMMAND_FLAG_TYPE)){
                                    val list = msgSplit.toMutableList()
                                    list.remove(msgSplit[0])
                                    args[it] = CommandPhraser.PhraseFlags(list)
                                }
                            }
                        }
                    } else if (!it.type.isMarkedNullable && !it.isOptional)
                        throw IllegalStateException("The type is not annotated and is not nullable")
                    else if (!it.isOptional)
                        args[it] = null
                }

                cmd.callBy(args)
            }
        }

    }

    fun discordUserToUser(user: IUser, guild: IGuild): User {
        return User(user.longID, guild.longID)
    }

    private fun List<String>.ToFormattedString() : String {
        var str = ""
        this.forEach {
            e -> str += "$e \n"
        }
        return str
    }

    private fun List<Permission>.toFormattedString() : String {
        var str = ""
        this.forEach { perm -> str += perm.permission + "\n"
        }
        return str
    }

    private fun List<Permission>.toStringList() : List<String> {
        var strList = mutableListOf<String>()
        this.forEach {
            strList.add(it.permission)
        }
        return strList
    }
}