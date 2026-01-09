package com.tbread

import com.tbread.entity.DpsData

class DpsCalculator(private val dataStorage: DataStorage) {

    enum class Mode{
        ALL,BOSS_ONLY
    }

    private var mode:Mode = Mode.BOSS_ONLY

    fun setMode(mode:Mode){
        this.mode = mode
    }
    
}