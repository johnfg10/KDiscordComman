package io.github.johnfg10

import io.github.johnfg10.command.Command
import io.github.johnfg10.permission.Permission
import io.github.johnfg10.storage.IPermmisionStorage
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

public open class KDiscordCommand(open val iPermisionStorage: IPermmisionStorage, open val defaultPrefix: String) {

    protected val classMap: MutableMap<KFunction<*>, KClass<*>> = mutableMapOf()

    protected val commandMap: MutableMap<Command, KFunction<*>> = mutableMapOf()

    protected val permissionMap: MutableMap<KFunction<*>, List<Permission>> = mutableMapOf()

    val guildPrefix: MutableMap<Guild, String> = mutableMapOf()

    public fun getClassMapp(): MutableMap<KFunction<*>, KClass<*>> {
        return classMap
    }

    public fun getCommandMapp(): MutableMap<Command, KFunction<*>> {
        return commandMap
    }

    public fun getPermissionMapp(): MutableMap<KFunction<*>, List<Permission>> {
        return permissionMap
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { this.shutdown() }))
    }

    public fun RegisterCommand(vararg  classes: KClass<*>){
        for (clazz in classes){
            clazz.functions.forEach {
                clazz.functions.filter { it.findAnnotation<Command>() != null }.forEach {
                    commandMap[it.findAnnotation<Command>()!!] = it
                    //we know this is a lit of permissions because of the filter arguments and the checks
                    val perms = it.annotations.filter { annotation -> annotation is Permission } as List<Permission>

                    if (perms.isNotEmpty()){
                        permissionMap[it] = perms
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