package com.daza.guitarsheetcreator.activities.models


class TablatureBar {
    companion object {
        private const val NUM_POSITIONS = 10
        private const val EMPTY_NOTE = "---"
    }

    val highE = MutableList(NUM_POSITIONS) { EMPTY_NOTE }
    val B     = MutableList(NUM_POSITIONS) { EMPTY_NOTE }
    val G     = MutableList(NUM_POSITIONS) { EMPTY_NOTE }
    val D     = MutableList(NUM_POSITIONS) { EMPTY_NOTE }
    val A     = MutableList(NUM_POSITIONS) { EMPTY_NOTE }
    val lowE  = MutableList(NUM_POSITIONS) { EMPTY_NOTE }
}
