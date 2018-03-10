package io.github.johnfg10.roles

import io.github.johnfg10.Guild
import io.github.johnfg10.roles.storage.IRoleStorage
import io.github.johnfg10.user.User

class RoleHandler(private val roleStorage: IRoleStorage) {
    private val shutdownHandler = Thread(Runnable { roleStorage.shutdown() })

    init {
        roleStorage.init()

        Runtime.getRuntime().addShutdownHook(shutdownHandler)
    }

    fun addRole(user: User, guild: Guild,  role: String) {
        roleStorage.addRoleToUser(guild, user, role)
    }

    fun removeRole(user: User, guild: Guild, role: String){
        roleStorage.removeRoleFromUser(guild, user, role)
    }

    fun checkRoles(user: User, guild: Guild, vararg roles: Role): Boolean {
        roles.forEach {
            if (!roleStorage.checkRole(guild, user, it.role)){
                return false
            }
        }

        return true
    }
}