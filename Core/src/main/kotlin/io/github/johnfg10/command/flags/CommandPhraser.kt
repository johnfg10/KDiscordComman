package io.github.johnfg10.command.flags

class CommandPhraser {
    companion object {
        /**
         *
         */
        fun PhraseFlags(arguments: List<String>) : List<CommandFlag> {
            val mutableList = mutableListOf<CommandFlag>()

            arguments.forEach {
                if (it.startsWith('-') && !it.contains(Regex("-{2,}"))){
                    val flagValue = it.replaceFirst("-", "")
                    mutableList.add(CommandFlag(flagValue))
                }
            }
            return mutableList
        }

        /**
         *
         */
        fun PhraseArguments(arguments: List<String>) : List<CommandArgument> {
            val mutableList = mutableListOf<CommandArgument>()
            arguments.forEach {
                if(it.startsWith("--")){
                    var argKey = it.replaceAfter("=", "")
                    argKey = argKey.replaceFirst("--", "")
                    argKey = argKey.replaceFirst("=", "")
                    //val argKey = it.removeRange(1, it.indexOfFirst { it == '=' })
                    val value = it.substring(it.indexOfFirst { it == '=' } + 1).replaceFirst("-=", "")
                    println("Arg: $argKey, Value: $value")
                    mutableList.add(CommandArgument(CommandFlag(argKey), value))
                }
/*
                if (it.startsWith("--") && !it.contains(Regex("-{3,}"))) {
                    val argKey: String
                    val argValue: String = it.replaceFirst(Regex("--\\S*="), "")
                    val argKeyResult = Regex("--\\S*=").find(it)
                    argKey = argKeyResult?.value?.replaceFirst("-", "")?.replaceFirst("=", "") ?: return emptyList()
                    mutableList.add(CommandArgument(CommandFlag(argKey), argValue))
                }*/
            }
            return mutableList
        }

    }
}