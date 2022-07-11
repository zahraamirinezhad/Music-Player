package com.example.musicplayer.Equalizer

class EqualizerModel {
    var isEqualizerEnabled = true
    var seekbarpos = IntArray(5)
    var presetPos = 0
    var reverbPreset: Short = -1
    var bassStrength: Short = -1

    init {
        reverbPreset = -1
        bassStrength = -1
    }
}