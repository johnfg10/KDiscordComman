package io.github.johnfg10.user

import io.github.johnfg10.Guild

abstract class AUser : Guild() {
    abstract val userId: Long
}