import ai.djl.Model
import ai.djl.inference.Predictor
import ai.djl.nn.Block
import ai.djl.translate.TranslateException
import ai.djl.translate.Translator
import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.deeplearn.DeepLearningEngine
import net.ccbluex.liquidbounce.deeplearn.DeepLearningEngine.modelsFolder
import java.io.Closeable
import java.nio.file.Path

private const val NUM_EPOCH = 100
private const val BATCH_SIZE = 32

abstract class BaseModelWrapper<I, O>(
    override val name: String,
    protected open val translator: Translator<I, O>,
    protected open val outputs: Long,
    override val parent: ChoiceConfigurable<*>,
) : Choice(name),
    Closeable {
    protected open val model: Model by lazy {
        Model.newInstance(name).apply {
            block = createModelBlock(outputs)
        }
    }
    protected open val predictor: Predictor<I, O> by lazy { model.newPredictor(translator) }

    abstract fun createModelBlock(outputs: Long): Block

    @Throws(TranslateException::class)
    open fun predict(input: I): O {
        require(DeepLearningEngine.isInitialized) { "DeepLearningEngine is not initialized" }
        return predictor.predict(input)
    }

    fun train(
        features: Array<FloatArray>,
        labels: Array<FloatArray>,
        numEpoch: Int = NUM_EPOCH,
        batchSize: Int = BATCH_SIZE,
        learningRate: Float = 0.001f,
    ) {
    }

    fun save(path: Path) {
        model.save(path, "tf")
    }

    fun save(name: String = this.name) {
        save(modelsFolder.resolve(name).toPath())
    }

    fun delete() {
        close()
        modelsFolder.resolve(name).delete()
    }

    override fun close() {
        predictor.close()
        model.close()
    }
}
