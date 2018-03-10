package io.github.johnfg10.roles.storage

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.github.johnfg10.Guild
import io.github.johnfg10.user.User
import java.io.File
import java.time.Instant
import kotlin.concurrent.timer

class JsonRoleStorage(val permission: File) : IRoleStorage {
    override fun checkRole(guild: Guild, user: User, role: String) : Boolean {
        if (guildMap.containsKey(guild)){
            if (guildMap[guild]!!.containsKey(user)){
                if (guildMap[guild]!![user]!!.contains(role)){
                    return true
                }
                if (guildMap[guild]!![user]!!.contains("*")){
                    return true
                }
            }
        }
        return false
    }

    var guildMap = mutableMapOf<Guild, MutableMap<User, MutableList<String>>>()
    val gson = Gson()

    fun ensureFileIsCreated(file: File){
        file.walkTopDown().forEach {
            if (!it.exists()){
                if (it.isDirectory)
                    it.mkdir()

                if (it.isFile)
                    it.createNewFile()
            }
        }
    }

    fun save(){
        permission.writeText(gson.toJson(guildMap))
    }

    override fun init() {
        if (!permission.exists()){
            ensureFileIsCreated(permission)

            permission.writeText(gson.toJson(guildMap))
        }else{
            if (permission.extension !== "json"){
                permission.renameTo(File(permission.parentFile, "permission-${Instant.now().epochSecond}"))
                ensureFileIsCreated(permission)
            }
            kotlin.run {
            repeat(2){
                fun loadFile(){
                    try {
                        guildMap = gson.fromJson(permission.readText(), (mutableMapOf<Guild, MutableMap<User, MutableList<String>>>())::class.java)
                        return
                    }catch (e: JsonSyntaxException){
                        if (it > 0)
                            throw e

                        println("${e.message} ${e.stackTrace}")
                    }
                }
            }
            }
        }

        timer("autosave", initialDelay = 0, period = 300000){
            save()
        }
    }

    override fun shutdown() {

        save()
    }

    override fun addRoleToUser(guild: Guild, user: User, role: String) {
        val map = guildMap.putIfAbsent(guild, mutableMapOf())
        val userPerms = map?.putIfAbsent(user, mutableListOf())
        if (userPerms?.contains(role)!!)
            return

        userPerms.add(role)
    }

    override fun removeRoleFromUser(guild: Guild, user: User, role: String) {
        val map = guildMap.putIfAbsent(guild, mutableMapOf())
        val userPerms = map?.putIfAbsent(user, mutableListOf())
        if (!userPerms?.contains(role)!!)
            return

        userPerms.remove(role)
    }
}