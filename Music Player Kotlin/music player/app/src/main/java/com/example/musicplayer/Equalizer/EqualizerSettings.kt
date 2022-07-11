package com.example.musicplayer.Equalizer

data class EqualizerSettings(
    var isEqualizerEnabled: Boolean = true,
    var isEqualizerReloaded: Boolean = true,
    var seekbarpos: IntArray = IntArray(5),
    var presetPos: Int = 0,
    var reverbPreset: Short = -1,
    var bassStrength: Short = -1,
    var equalizerModel: EqualizerModel? = null,
    var ratio: Double = 1.0,
    var isEditing: Boolean = false
)