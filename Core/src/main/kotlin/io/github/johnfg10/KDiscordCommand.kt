package io.github.johnfg10

import io.github.johnfg10.command.Command
import io.github.johnfg10.permission.Permission
import io.github.johnfg10.storage.IPermmisionStorage
import io.github.johnfg10.storage.JsonPermissionStore
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

open public class KDiscordCommand(val iPermisionStorage: IPermmisionStorage, val defaultPrefix: String) {

    protected val classMap: MutableMap<KFunction<*>, KClass<*>> = mutableMapOf()

    protected val commandMap: MutableMap<Command, KFunction<*>> = mutableMapOf()

    protected val permissionMap: MutableMap<KFunction<*>, Permission> = mutableMapOf()

    protected val guildPrefix: MutableMap<Guild, String> = mutableMapOf()

    init {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { this.shutdown() }))
    }

    constructor() : this("+")

    constructor(defaultPrefix: String) : this(File("./permissions.json"), defaultPrefix)

    constructor(file: File, defaultPrefix: String) : this(JsonPermissionStore(file), defaultPrefix)

    public fun RegisterCommand(vararg  classes: KClass<*>){
        for (clazz in classes){
            clazz.functions.forEach {
                clazz.functions.filter { it.findAnnotation<Command>() != null }.forEach {
                    commandMap[it.findAnnotation<Command>()!!] = it
                    val permission = it.findAnnotation<Permission>()
                    if (permission != null){
                        permissionMap[it] = permission
                    }
                }

                val command = it.findAnnotation<Command>()
                if (command != null){
                    commandMap[command] = it
                    val permission = it.findAnnotation<Permission>()
                    if (permission != null){
                        permissionMap[it] = permission
                    }
                }

                classMap[it] = clazz
            }
        }
    }

    public fun UnRegisterCommand(vararg classes: KClass<*>){
        for (clazz in classes){
            clazz.functions.forEach {
                clazz.functions.filter { it.findAnnotation<Command>() != null }.forEach {
                    commandMap.remove(it.findAnnotation<Command>())
                    if (permissionMap.containsKey(it))
                        permissionMap.remove(it)

                    classMap.remove(it)
                }
            }
        }
    }

    operator fun plus(clazz: KClass<*>) = RegisterCommand(clazz)

    operator fun minus(clazz: KClass<*>) = UnRegisterCommand(clazz)

    public fun shutdown(){
        iPermisionStorage.shutdown()
    }
}