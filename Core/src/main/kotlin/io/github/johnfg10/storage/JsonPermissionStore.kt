package io.github.johnfg10.storage

import com.google.gson.Gson
import io.github.johnfg10.user.AUser
import io.github.johnfg10.permission.AUserPermission
import java.io.File

/**
 * Very simplistic store used to store permission data
 */
class JsonPermissionStore(private val location: File) : IPermmisionStorage {

    val userPermissionStore = mutableListOf<AUserPermission>()

    override fun addUserPermission(userPermission: AUserPermission) {
        userPermissionStore.add(userPermission)
    }

    override fun removeUserPermission(userPermission: AUserPermission) {
        userPermissionStore.remove(userPermission)
    }

    override fun updateUserPermission(userPermission: AUserPermission) {

    }

    override fun findUserPermission(AUser: AUser): AUserPermission? {
        return userPermissionStore.filter { it == AUser }.firstOrNull()
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