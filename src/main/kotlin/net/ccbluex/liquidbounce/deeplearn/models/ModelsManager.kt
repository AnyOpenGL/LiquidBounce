package net.ccbluex.liquidbounce.deeplearn.models

object ModelsManager {
    val modelTypes =
        mapOf(
            "mlp" to MinaraiModelMLP::class.java,
            "lstm" to MinaraiModelLSTM::class.java,
        )
}
