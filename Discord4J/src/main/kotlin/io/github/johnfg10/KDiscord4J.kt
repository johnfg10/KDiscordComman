package io.github.johnfg10

import io.github.johnfg10.command.CommandArg
import io.github.johnfg10.command.CommandArgumentType
import io.github.johnfg10.command.flags.CommandFlag
import io.github.johnfg10.command.flags.CommandPhraser
import io.github.johnfg10.user.User
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.*

class KDiscord4J(val discordClient: IDiscordClient) : KDiscordCommand() {

    val IUSER_TYPE = IUser::class.createType()
    val ICHANNEL_TYPE = IChannel::class.createType()
    val IGUILD_TYPE = IGuild::class.createType()
    val IMESSAGE_TYPE = IMessage::class.createType()
    val COMMAND_ARGUMENT = CommandArg::class.createType()
    val COMMAND_FLAG = CommandFlag::class.createType()
    val STRING_TYPE = String::class.createType()
    val LIST_IUSER_TYPE = MutableList::class.createType(listOf(KTypeProjection(null, IUSER_TYPE)))
    val ARRAY_IUSER_TYPE = Array<IUser>::class.createType(listOf(KTypeProjection(null, IUSER_TYPE)))
    val LIST_COMMAND_FLAG_TYPE = MutableList::class.createType(listOf(KTypeProjection(null, COMMAND_FLAG)))
    val LIST_COMMAND_ARGUMENT_TYPE = MutableList::class.createType(listOf(KTypeProjection(null, COMMAND_ARGUMENT)))


    init{
        discordClient.dispatcher.registerListener(this)
    }

    @EventSubscriber
    fun onMessageReceivedEvent(msg: MessageReceivedEvent){
        val message = msg.message.content
        val user = discordUserToUser(msg.author, msg.guild)

        val prefix: String = guildPrefix[user] ?: defaultPrefix

        if(message == "DEBUGINFO"){
            msg.channel.sendMessage(
                    "command map size: ${commandMap.size}\n" +
                            "Permission map size: ${permissionMap.size}"
            )
            if (commandMap.isNotEmpty()){
                msg.channel.sendMessage(commandMap.toString())
            }
        }



        if (message.startsWith(prefix)){
            val msgSplit = message.split(" ")

            val msgCommand = msgSplit[0].replace(prefix, "")

            println(msgCommand)
            val cmdMap = commandMap.filter { it.key.aliases.contains(msgCommand) }
            if (cmdMap.isEmpty())
                return

            val cmdKey = cmdMap.keys.first()
            val cmd = cmdMap[cmdKey]

            if (cmd != null){
                val args = mutableMapOf<KParameter, Any?>()

                args[cmd.instanceParameter!!] = classMap[cmd]!!.createInstance()

                for(it in cmd.valueParameters){
                    //println("name: ${it.name} Type: ${it.type} Kind: ${it.kind}")
                    val cmdAnnotation = it.findAnnotation<CommandArg>()
                    if (cmdAnnotation != null){
/*                        println("none null cmd found")
                        println(cmdAnnotation.type)*/
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

}