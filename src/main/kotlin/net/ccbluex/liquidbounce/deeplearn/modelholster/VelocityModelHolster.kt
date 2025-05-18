package net.ccbluex.liquidbounce.deeplearn.modelholster

import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.deeplearn.DeepLearningEngine
import net.ccbluex.liquidbounce.deeplearn.models.minaraimodel.MinaraiModel
import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleClickGui
import net.ccbluex.liquidbounce.utils.client.logger
import net.ccbluex.liquidbounce.utils.kotlin.mapArray
import kotlin.time.measureTime

object VelocityModelHolster {
    object MinaraiModelHolster : EventListener, Configurable("MinaraiModel") {
        /**
         * Base models that are always available
         * and are included in the LiquidBounce JAR.
         *
         * The name can contain uppercase characters,
         * but the file should always be lowercase.
         */
        val baseModels = arrayOf<String>()

        val modelsFolder = DeepLearningEngine.modelsFolder.resolve("velocity").apply { mkdir() }

        /**
         * Available models from the models folder
         */
        private val availableModels: List<String>
            get() =
                modelsFolder
                    .listFiles { file -> file.isDirectory }
                    ?.map { file -> file.nameWithoutExtension } ?: emptyList()

        private val allModels: Array<String>
            get() = baseModels + availableModels

        val models =
            choices(this, "Model", 0) { choiceConfigurable ->
                // Empty models for start-up initialization.
                // These will be replaced later on at [load].
                allModels.mapArray { name ->
                    MinaraiModel(name, choiceConfigurable)
                }
            }

        /**
         * Load models from the models folder. This only has to be triggered
         * when reloading the models. Otherwise, the models are loaded on startup
         * through the choice initialization.
         */
        fun load() {
            logger.info("[DeepLearning] Loading models...")
            val choices =
                allModels.map { name ->
                    MinaraiModel(name, models)
                }

            for (model in choices) {
                runCatching {
                    measureTime {
                        model.load()
                    }
                }.onFailure { error ->
                    logger.error("[DeepLearning] Failed to load model '${model.name}'.", error)
                }.onSuccess { time ->
                    logger.info("[DeepLearning] Loaded model '${model.name}' in ${time.inWholeMilliseconds}ms.")
                }
            }

            models.choices = choices.toMutableList()
            models.setByString(models.activeChoice.name)
            ModuleClickGui.reloadView()
        }

        /**
         * Unload all models.
         */
        fun unload() {
            val iterator = models.choices.iterator()

            while (iterator.hasNext()) {
                val model = iterator.next()
                model.close()
                iterator.remove()
            }
        }

        /**
         * Clear out all models and load-in the models again.
         */
        fun reload() {
            unload()
            load()
        }
    }
}
