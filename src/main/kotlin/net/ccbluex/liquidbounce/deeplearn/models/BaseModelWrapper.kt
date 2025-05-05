import ai.djl.Model
import ai.djl.inference.Predictor
import ai.djl.nn.Block
import ai.djl.translate.TranslateException
import ai.djl.translate.Translator
import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.deeplearn.DeepLearningEngine
import net.ccbluex.liquidbounce.deeplearn.DeepLearningEngine.modelsFolder
import okio.Path.Companion.toPath
import java.io.Closeable
import java.io.InputStream
import java.nio.file.Path
import java.util.Locale

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

    abstract val typeName: String

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

    fun load(stream: InputStream) {
        model.load(stream)
    }

    fun load(path: Path) {
        model.load(path, "tf")
    }

    fun load(name: String = this.name) {
        val folder = modelsFolder.resolve(name)

        if (folder.exists()) {
            load(folder.toPath())
        } else {
            val lowercaseName = name.lowercase(Locale.ENGLISH)
            javaClass.getResourceAsStream("/resources/liquidbounce/models/$lowercaseName.params")!!.use { stream ->
                load(stream)
            }
        }
    }

    open fun save(path: Path) {
        model.save(path, "tf")
    }

    fun save(name: String = this.name) {
        save(modelsFolder.resolve(typeName).resolve(name).toPath())
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
