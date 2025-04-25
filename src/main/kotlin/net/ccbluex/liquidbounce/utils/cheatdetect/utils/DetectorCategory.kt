package net.ccbluex.liquidbounce.utils.cheatdetect.utils

import net.ccbluex.liquidbounce.features.module.Category

enum class DetectorCategory(val readableName: String) {

    None("None"),
    Motion("Motion"),
    Combat("Combat");


    companion object {
        /**
         * Gets an enum by its readable name
         */
        fun fromReadableName(name: String): Category? {
            return Category.entries.find { name.equals(it.name, true) }
        }

        fun getReadableName(detectorCategory: DetectorCategory): String {
            return detectorCategory.name
        }
    }
}
