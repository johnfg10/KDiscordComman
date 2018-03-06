package io.github.johnfg10.storage

import com.google.gson.Gson
import io.github.johnfg10.permission.AUserPermission
import io.github.johnfg10.permission.PermissionList
import io.github.johnfg10.user.User
import java.io.File
import java.time.Instant
import java.util.*

/**
 * Very simplistic store used to store permission data
 */
class JsonPermissionStore(private val location: File) : IPermmisionStorage {
    var userPermissionStore = PermissionList(mutableListOf())
    var retryAttempts = 0

    init {
        if (!location.exists())
            location.createNewFile()

        if (location.exists()){
            load()

            userPermissionStore.permissions.forEach { println("ups entry Guild ID: ${it.guildId} User ID: ${it.userId} " +
                    "Perms: ${it.permissions.joinToString("\n")}") }
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

    override fun removeUserPermission(user: User, permission: String) {
        val userPerm = findUserPermission(user)
        if (userPerm != null){
            userPerm.permissions.remove(permission)
            userPermissionStore.permissions.remove(userPerm)
            userPermissionStore.permissions.add(userPerm)
        }
    }


    override fun findUserPermission(user: User): AUserPermission? {
        return userPermissionStore.permissions.firstOrNull { it.userId == user.userId && it.guildId == user.guildId }
    }

    override fun load() {
        val gson = Gson()

        try{
            userPermissionStore = gson.fromJson<PermissionList>(location.readText(),
                    PermissionList::class.java)
        }catch (e: IllegalStateException) {
            if (retryAttempts > 1)
                throw e
            println(e.message)

            val pathname = location.absolutePath.replace(location.name, "")

            location.renameTo(File(pathname + "permissions-backup-${Instant.now().toEpochMilli()}.json"))

            println(location.path)

            if (!location.exists())
                location.createNewFile()

            retryAttempts++
        }
    }

    override fun save() {
        val gson = Gson()
        val userStoreJson = gson.toJson(userPermissionStore)
        location.writeText(userStoreJson)
    }

    override fun shutdown() {
        this.save()
    }
}