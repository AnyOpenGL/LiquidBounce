package net.ccbluex.liquidbounce.deeplearn.models

object ModelsManager {
    val modelTypes =
        arrayOf(
            MinaraiModelLSTM::class,
            MinaraiModelMLP::class,
        )
}
