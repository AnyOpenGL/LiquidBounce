package net.ccbluex.liquidbounce.features.command.commands.module

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandException
import net.ccbluex.liquidbounce.features.command.CommandFactory
import net.ccbluex.liquidbounce.features.command.builder.CommandBuilder
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleBotTest
import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleTeleport
import net.ccbluex.liquidbounce.utils.bot.BotManager.botChat
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.player
import net.minecraft.util.math.Vec3d

object CommandLiquidBot : CommandFactory{
    override fun createCommand(): Command {
        return CommandBuilder
            .begin("bot")
            .requiresIngame()
            .hub()
            .subcommand(gotoCommand())
            .build()

    }

    private fun gotoCommand() = CommandBuilder
        .begin("goto")
        .parameter(
            ParameterBuilder
                .begin<Float>("x")
                .required()
                .build(),
        )
        .parameter(
            ParameterBuilder
                .begin<Float>("y|z")
                .required()
                .build()
        )
        .parameter(
            ParameterBuilder
                .begin<Float>("z")
                .optional()
                .build()
        )
        .handler { command, args ->
            val x = (args[0] as String).toDoubleOrNull()
            val z = (args[args.size - 1] as String).toDoubleOrNull()
            val y = (args[1] as String).toDoubleOrNull()


            if (x == null || y == null || z == null) {
                throw CommandException(command.result("invalidCoordinates"))
            }


            ModuleBotTest.goto(Vec3d(x,y,z))
        }
        .build()




}
