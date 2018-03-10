package io.github.johnfg10

import io.github.johnfg10.command.Command
import io.github.johnfg10.command.CommandController
import io.github.johnfg10.roles.Role
import io.github.johnfg10.roles.RoleHandler
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import jdk.nashorn.internal.codegen.CompilerConstants.className

open class KDiscordCommand(open val roleHandler: RoleHandler, open val defaultPrefix: String) {

    protected val classMap: MutableMap<KFunction<*>, KClass<*>> = mutableMapOf()

    protected val commandMap: MutableMap<Command, KFunction<*>> = mutableMapOf()

    protected val roleMap: MutableMap<KFunction<*>, List<Role>> = mutableMapOf()

    val guildPrefix: MutableMap<Guild, String> = mutableMapOf()

    public fun getClassMapp(): MutableMap<KFunction<*>, KClass<*>> {
        return classMap
    }

    public fun getCommandMapp(): MutableMap<Command, KFunction<*>> {
        return commandMap
    }

    public fun getRoleMapp(): MutableMap<KFunction<*>, List<Role>> {
        return roleMap
    }

    init {
        val scanner = FastClasspathScanner("").scan()

        val classes = scanner.classNamesToClassRefs(scanner.getNamesOfClassesWithAnnotation(CommandController::class.java))
        classes.forEach {
            RegisterCommand(it.kotlin)
        }

    }

    public fun RegisterCommand(vararg  classes: KClass<*>) {
        for (clazz in classes) {
            clazz.functions.forEach {
                clazz.functions.filter { it.findAnnotation<Command>() != null }.forEach {
                    commandMap[it.findAnnotation<Command>()!!] = it
                    //we know this is a lit of permissions because of the filter arguments and the checks
/*                    val perms = it.annotations.filter { annotation -> annotation is Permission } as List<Permission>

                    if (perms.isNotEmpty()){
                        permissionMap[it] = perms
                    }*/
                    if (it.annotations.any { it is Role }) {
                        roleMap[it] = it.annotations.filterIsInstance<Role>()
                    }

                    classMap[it] = clazz
                }
            }
        }
    }

    public fun UnRegisterCommand(vararg classes: KClass<*>){
        for (clazz in classes){
            clazz.functions.forEach {
                clazz.functions.filter { it.findAnnotation<Command>() != null }.forEach {
                    commandMap.remove(it.findAnnotation<Command>())
/*                    if (permissionMap.containsKey(it))
                        permissionMap.remove(it)*/

                    if (roleMap.containsKey(it))
                        roleMap.remove(it)



                    classMap.remove(it)
                }
            }
        }
    }

    operator fun plus(clazz: KClass<*>) = RegisterCommand(clazz)

    operator fun minus(clazz: KClass<*>) = UnRegisterCommand(clazz)
}