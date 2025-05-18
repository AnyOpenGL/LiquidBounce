package net.ccbluex.liquidbounce.deeplearn.models

import ai.djl.Model
import ai.djl.ndarray.types.Shape
import ai.djl.training.DefaultTrainingConfig
import ai.djl.training.EasyTrain
import ai.djl.training.dataset.ArrayDataset
import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.deeplearn.DeepLearningEngine
import net.ccbluex.liquidbounce.deeplearn.models.minaraimodel.NUM_EPOCH
import net.ccbluex.liquidbounce.features.command.Command

private const val NUM_EPOCH = 100
private const val BATCH_SIZE = 32

abstract class ModelWrapper(
    name: String,
) : Choice(name) {
    abstract val model: Model

    abstract fun train(command: Command)

    fun train(
        trainingConfig: DefaultTrainingConfig,
        shape: Shape,
        trainingDataSet: ArrayDataset,
        validationDataSet: ArrayDataset? = null,
    ) {
        require(DeepLearningEngine.isInitialized) { "DeepLearningEngine is not initialized" }

        val trainer = model.newTrainer(trainingConfig)
        trainer.initialize(shape)
        EasyTrain.fit(trainer, NUM_EPOCH, trainingDataSet, validationDataSet)
    }
}
