package net.ccbluex.liquidbounce.deeplearn.models

import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.deeplearn.translators.FloatArrayInAndOutTranslator

class MinaraiModelLSTM(
    name: String,
    parent: ChoiceConfigurable<*>,
) : ModelWrapperLSTM<FloatArray, FloatArray>(
        name,
        FloatArrayInAndOutTranslator(),
        2, // X, Y
        parent,
    )
