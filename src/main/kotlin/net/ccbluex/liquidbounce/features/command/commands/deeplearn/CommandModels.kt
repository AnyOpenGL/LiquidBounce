/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */
package net.ccbluex.liquidbounce.features.command.commands.deeplearn

import net.ccbluex.liquidbounce.deeplearn.DeepLearningEngine.modelsFolder
import net.ccbluex.liquidbounce.deeplearn.ModelCategory
import net.ccbluex.liquidbounce.deeplearn.modelholster.MinaraiModelHolster
import net.ccbluex.liquidbounce.deeplearn.modelholster.MinaraiModelHolster.models
import net.ccbluex.liquidbounce.deeplearn.models.minaraimodel.MinaraiModel
import net.ccbluex.liquidbounce.deeplearn.models.velocitymodel.VelocityModel
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandException
import net.ccbluex.liquidbounce.features.command.CommandFactory
import net.ccbluex.liquidbounce.features.command.builder.CommandBuilder
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleClickGui
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.markAsError
import net.ccbluex.liquidbounce.utils.client.regular
import net.ccbluex.liquidbounce.utils.client.variable
import net.minecraft.util.Util
import kotlin.concurrent.thread
import kotlin.time.DurationUnit
import kotlin.time.measureTime

object CommandModels : CommandFactory {
    override fun createCommand(): Command =
        CommandBuilder
            .begin("models")
            .hub()
            .subcommand(createModelCommand())
            .subcommand(improveModelCommand())
            .subcommand(deleteModelCommand())
            .subcommand(reloadModelCommand())
            .subcommand(browseModelCommand())
            .build()

    private fun createModelCommand(): Command =
        CommandBuilder
            .begin("create")
            .parameter(
                ParameterBuilder
                    .begin<String>("type")
                    .required()
                    .autocompletedWith { begin, _ -> ModelCategory.entries.map { it.name } }
                    .build(),
            ).parameter(
                ParameterBuilder
                    .begin<String>("name")
                    .required()
                    .build(),
            ).handler { command, args ->
                val type = args[0] as String
                val name = args[1] as String

                // Check if model exists
                if (models.choices.any { model -> model.name.equals(name, true) }) {
                    throw CommandException(command.result("modelExists", name))
                }

                // Check if the name is a valid name
                if (name.contains(Regex("[^a-zA-Z0-9-]"))) {
                    throw CommandException(command.result("invalidName"))
                }

                chat(command.result("trainingStart", name))
                thread {
                    trainModel(command, name, type)
                }
            }.build()

    private fun improveModelCommand(): Command =
        CommandBuilder
            .begin("improve")
            .parameter(
                ParameterBuilder
                    .begin<String>("name")
                    .required()
                    .build(),
            ).handler { command, args ->
                val name = args[0] as String
                val model =
                    models.choices.find { model -> model.name.equals(name, true) }
                        ?: throw CommandException(command.result("modelNotFound", name))

                chat(command.result("trainingStart", name))
                thread {
                    measureTime {
                        model.train(command)
                        model.save()
                        models.setByString(model.name)
                        ModuleClickGui.reloadView()
                    }
                }
            }.build()

    private fun deleteModelCommand(): Command {
        return CommandBuilder
            .begin("delete")
            .parameter(
                ParameterBuilder
                    .begin<String>("name")
                    .required()
                    .build(),
            ).handler { command, args ->
                val name = args[0] as String
                val model = models.choices.find { model -> model.name.equals(name, true) }

                if (model == null) {
                    chat(markAsError(command.result("modelNotFound", name)))
                    return@handler
                }

                model.delete()
                models.choices.remove(model)
                chat(command.result("modelDeleted", name))
            }.build()
    }

    private fun reloadModelCommand(): Command =
        CommandBuilder
            .begin("reload")
            .handler { command, _ ->
                MinaraiModelHolster.reload()
                chat(command.result("modelsReloaded"))
            }.build()

    private fun browseModelCommand(): Command =
        CommandBuilder
            .begin("browse")
            .handler { command, _ ->
                Util.getOperatingSystem().open(modelsFolder)
                chat(regular("Location: "), variable(modelsFolder.absolutePath))
            }.build()

    private fun trainModel(
        command: Command,
        name: String,
        type: String,
    ) {
        val trainingTime =
            measureTime {
                val model =
                    when (type) {
                        "Minarai" -> MinaraiModel(name, models)
                        "Velocity" -> VelocityModel(name, models)
                        else -> throw CommandException(command.result("invalidType", type))
                    }

                model.train(command)
                model.save()

                models.setByString(model.name)
                ModuleClickGui.reloadView()
            }

        chat(command.result("trainingEnd", name, trainingTime.toString(DurationUnit.MINUTES, decimals = 2)))
    }
}
