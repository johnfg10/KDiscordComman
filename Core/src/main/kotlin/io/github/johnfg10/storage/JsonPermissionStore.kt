package io.github.johnfg10.storage

import com.google.gson.Gson
import io.github.johnfg10.permission.AUserPermission
import io.github.johnfg10.permission.PermissionList
import io.github.johnfg10.user.User
import java.io.File

/**
 * Very simplistic store used to store permission data
 */
class JsonPermissionStore(private val location: File) : IPermmisionStorage {

    //var userPermissionStore = mutableListOf<AUserPermission>()
    var userPermissionStore = PermissionList(mutableListOf())

    init {
        if (location.exists()){
            load()

            println(location.readText())

            userPermissionStore.permissions.forEach { println("ups entry Guild ID: ${it.guildId} User ID: ${it.userId} " +
                    "sPerms: ${it.permissions.joinToString("\n")}") }
        }
    }

    override fun giveUserPermission(user: User, permission: String) {
        val userPerm = findUserPermission(user)
        if (userPerm != null){
            userPerm.permissions.add(permission)
            userPermissionStore.permissions.remove(userPerm)
            userPermissionStore.permissions.add(userPerm)
        }else{
            userPermissionStore.permissions.add(AUserPermission(user.guildId, user.userId, mutableListOf(permission)))
        }
    }

    override fun findUserPermission(user: User): AUserPermission? {
        return userPermissionStore.permissions.firstOrNull { it.userId == user.userId && it.guildId == user.guildId }
    }

    override fun load() {
        val gson = Gson()
        userPermissionStore = gson.fromJson<PermissionList>(location.readText(),
                PermissionList::class.java)
    }

    override fun save() {
        val gson = Gson()
        val userStoreJson = gson.toJson(userPermissionStore)
        location.writeText(userStoreJson)
    }

    override fun shutdown() {
        this.save()
    }

    init {
        if (!location.exists())
            location.createNewFile()
    }
}