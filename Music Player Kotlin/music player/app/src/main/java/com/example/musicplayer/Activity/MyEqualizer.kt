package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.db.chart.model.LineSet
import com.db.chart.view.AxisController
import com.db.chart.view.ChartView
import com.db.chart.view.LineChartView
import com.example.musicplayer.Equalizer.AnalogController
import com.example.musicplayer.Equalizer.EqualizerModel
import com.example.musicplayer.Equalizer.EqualizerSettings
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityMyEqualizerBinding

class MyEqualizer : AppCompatActivity() {
    lateinit var mEqualizer: Equalizer
    private lateinit var equalizerSwitch: SwitchCompat
    lateinit var bassBoost: BassBoost
    lateinit var chart: LineChartView
    lateinit var presetReverb: PresetReverb
    private lateinit var backBtn: ImageView
//    private lateinit var spinnerDropDownIcon: ImageView
    var y = 0
    private lateinit var fragTitle: TextView
    private lateinit var mLinearLayout: LinearLayout
    var seekBarFinal = arrayOfNulls<SeekBar>(5)
    private lateinit var bassController: AnalogController
    private lateinit var reverbController: AnalogController
//    lateinit var presetSpinner: Spinner
    lateinit var dataset: LineSet
    private lateinit var paint: Paint
    lateinit var points: FloatArray
    private var numberOfFrequencyBands: Short = 0
    private var audioSessionId = 0
    var settings: EqualizerSettings = EqualizerSettings()
    lateinit var binding: ActivityMyEqualizerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityMyEqualizerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()

        equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->
            mEqualizer.enabled = isChecked
            bassBoost.enabled = isChecked
            presetReverb.enabled = isChecked
            settings.isEqualizerEnabled = isChecked
            settings.equalizerModel!!.isEqualizerEnabled = isChecked
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }

//        spinnerDropDownIcon.setOnClickListener { presetSpinner.performClick() }

        bassController.setOnProgressChangedListener(object :
            AnalogController.onProgressChangedListener {
            override fun onProgressChanged(progress: Int) {
                settings.bassStrength = (1000.toFloat() / 19 * progress).toInt().toShort()
                try {
                    bassBoost.setStrength(settings.bassStrength)
                    settings.equalizerModel!!.bassStrength = settings.bassStrength
                } catch (e: Exception) {
                    Toast.makeText(this@MyEqualizer, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        })

        reverbController.setOnProgressChangedListener(object :
            AnalogController.onProgressChangedListener {
            override fun onProgressChanged(progress: Int) {
                settings.reverbPreset = (progress * 6 / 19).toShort()
                settings.equalizerModel!!.reverbPreset = settings.reverbPreset
                try {
                    presetReverb.preset = settings.reverbPreset
                } catch (e: Exception) {
                    Toast.makeText(this@MyEqualizer, e.message.toString(), Toast.LENGTH_LONG).show()
                }
                y = progress
            }
        })
    }

    @SuppressLint("SetTextI18n")
    fun initialize() {
        audioSessionId = Player.musicService!!.mediaPlayer!!.audioSessionId
        Player.musicService!!.mediaPlayer!!.isLooping = true
        settings.isEditing = true

        if (settings.equalizerModel == null) {
            settings.equalizerModel = EqualizerModel()
            settings.equalizerModel!!.reverbPreset = PresetReverb.PRESET_NONE
            settings.equalizerModel!!.bassStrength = (1000 / 19).toShort()
        }
        mEqualizer = Equalizer(0, audioSessionId)
        bassBoost = BassBoost(0, audioSessionId)
        bassBoost.enabled = settings.isEqualizerEnabled
        val bassBoostSettingTemp = bassBoost.properties
        val bassBoostSetting = BassBoost.Settings(bassBoostSettingTemp.toString())
        bassBoostSetting.strength = settings.equalizerModel!!.bassStrength
        bassBoost.properties = bassBoostSetting
        presetReverb = PresetReverb(0, audioSessionId)
        presetReverb.preset = settings.equalizerModel!!.reverbPreset
        presetReverb.enabled = settings.isEqualizerEnabled
        mEqualizer.enabled = settings.isEqualizerEnabled
        if (settings.presetPos == 0) {
            for (bandIdx in 0 until mEqualizer.numberOfBands) {
                mEqualizer.setBandLevel(
                    bandIdx.toShort(), settings.seekbarpos[bandIdx].toShort()
                )
            }
        } else {
            mEqualizer.usePreset(settings.presetPos.toShort())
        }

        backBtn = binding.equalizerBackBtn
        backBtn.visibility = if (showBackButton) View.VISIBLE else View.GONE

        fragTitle = binding.equalizerFragmentTitle
        equalizerSwitch = binding.equalizerSwitch
        equalizerSwitch.isChecked = settings.isEqualizerEnabled

//        spinnerDropDownIcon = binding.spinnerDropdownIcon


//        presetSpinner = binding.equalizerPresetSpinner

        chart = binding.lineChart
        paint = Paint()
        dataset = LineSet()
        bassController = binding.controllerBass
        reverbController = binding.controller3D
        bassController.label = "BASS"
        reverbController.label = "3D"
        bassController.circlePaint2.color = themeColor
        bassController.linePaint.color = themeColor
        bassController.invalidate()
        reverbController.circlePaint2.color = themeColor
        bassController.linePaint.color = themeColor
        reverbController.invalidate()
        if (!settings.isEqualizerReloaded) {
            var x = 0
            try {
                x = bassBoost.roundedStrength * 19 / 1000
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                y = presetReverb.preset * 19 / 6
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (x == 0) {
                bassController.progress = 1
            } else {
                bassController.progress = x
            }
            if (y == 0) {
                reverbController.progress = 1
            } else {
                reverbController.progress = y
            }
        } else {
            val x: Int = settings.bassStrength * 19 / 1000
            y = settings.reverbPreset * 19 / 6
            if (x == 0) {
                bassController.progress = 1
            } else {
                bassController.progress = x
            }
            if (y == 0) {
                reverbController.progress = 1
            } else {
                reverbController.progress = y
            }
        }

        mLinearLayout = binding.equalizerContainer
        numberOfFrequencyBands = 5
        points = FloatArray(numberOfFrequencyBands.toInt())
        val lowerEqualizerBandLevel = mEqualizer.bandLevelRange[0]
        val upperEqualizerBandLevel = mEqualizer.bandLevelRange[1]
        for (i in 0 until numberOfFrequencyBands) {
            val equalizerBandIndex = i.toShort()
            val frequencyHeaderTextView = TextView(this)
            frequencyHeaderTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            frequencyHeaderTextView.gravity = Gravity.CENTER_HORIZONTAL
            frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"))
            frequencyHeaderTextView.text =
                (mEqualizer.getCenterFreq(equalizerBandIndex) / 1000).toString() + "Hz"
            val seekBarRowLayout = LinearLayout(this)
            seekBarRowLayout.orientation = LinearLayout.VERTICAL
            val lowerEqualizerBandLevelTextView = TextView(this)
            lowerEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"))
            lowerEqualizerBandLevelTextView.text = (lowerEqualizerBandLevel / 100).toString() + "dB"
            val upperEqualizerBandLevelTextView = TextView(this)
            lowerEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"))
            upperEqualizerBandLevelTextView.text = (upperEqualizerBandLevel / 100).toString() + "dB"
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.weight = 1f
            var seekBar = SeekBar(this)
            var textView = TextView(this)
            when (i) {
                0 -> {
                    seekBar = findViewById(R.id.seekBar1)
                    textView = findViewById(R.id.textView1)
                }
                1 -> {
                    seekBar = findViewById(R.id.seekBar2)
                    textView = findViewById(R.id.textView2)
                }
                2 -> {
                    seekBar = findViewById(R.id.seekBar3)
                    textView = findViewById(R.id.textView3)
                }
                3 -> {
                    seekBar = findViewById(R.id.seekBar4)
                    textView = findViewById(R.id.textView4)
                }
                4 -> {
                    seekBar = findViewById(R.id.seekBar5)
                    textView = findViewById(R.id.textView5)
                }
            }
            seekBarFinal[i] = seekBar
            seekBar.progressDrawable.colorFilter =
                PorterDuffColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN)
            seekBar.thumb.colorFilter =
                PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN)
            seekBar.id = i

            seekBar.max = upperEqualizerBandLevel - lowerEqualizerBandLevel
            textView.text = frequencyHeaderTextView.text
            textView.setTextColor(Color.WHITE)
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            if (settings.isEqualizerReloaded) {
                points[i] = (settings.seekbarpos[i] - lowerEqualizerBandLevel).toFloat()
                dataset.addPoint(frequencyHeaderTextView.text.toString(), points[i])
                seekBar.progress = settings.seekbarpos[i] - lowerEqualizerBandLevel
            } else {
                points[i] =
                    (mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel).toFloat()
                dataset.addPoint(frequencyHeaderTextView.text.toString(), points[i])
                seekBar.progress =
                    mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel
                settings.seekbarpos[i] = mEqualizer.getBandLevel(equalizerBandIndex).toInt()
                settings.isEqualizerReloaded = true
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mEqualizer.setBandLevel(
                        equalizerBandIndex,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                    points[seekBar.id] =
                        (mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel).toFloat()
                    settings.seekbarpos[seekBar.id] =
                        progress + lowerEqualizerBandLevel
                    settings.equalizerModel!!.seekbarpos[seekBar.id] =
                        progress + lowerEqualizerBandLevel
                    dataset.updateValues(points)
                    chart.notifyDataUpdate()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
//                    presetSpinner.setSelection(0)
                    settings.presetPos = 0
                    settings.equalizerModel!!.presetPos = 0
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        equalizeSound()
        paint.color = Color.parseColor("#555555")
        paint.strokeWidth = (1.10 * settings.ratio).toFloat()
        dataset.color = themeColor
        dataset.isSmooth = true
        dataset.thickness = 5f
        chart.setXAxis(false)
        chart.setYAxis(false)
        chart.setYLabels(AxisController.LabelPosition.NONE)
        chart.setXLabels(AxisController.LabelPosition.NONE)
        chart.setGrid(ChartView.GridType.NONE, 7, 10, paint)
        chart.setAxisBorderValues(-300, 3300)
        chart.addData(dataset)
        chart.show()
        val mEndButton = Button(this)
        mEndButton.setBackgroundColor(themeColor)
        mEndButton.setTextColor(Color.WHITE)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Player.musicService!!.mediaPlayer!!.isLooping = false
    }

    private fun equalizeSound() {
        val equalizerPresetNames: ArrayList<String?> = ArrayList()
//        val equalizerPresetSpinnerAdapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
//            this,
//            R.layout.spinner_item,
//            equalizerPresetNames as List<Any?>
//        )
        equalizerPresetNames.add("Custom")
        for (i in 0 until mEqualizer.numberOfPresets) {
            equalizerPresetNames.add(mEqualizer.getPresetName(i.toShort()))
        }
//        presetSpinner.adapter = equalizerPresetSpinnerAdapter
//        presetSpinner.setDropDownWidth((Settings.screen_width * 3) / 4);
//        if (settings.isEqualizerReloaded && settings.presetPos != 0) {
//            correctPosition = false;
//            presetSpinner.setSelection(settings.presetPos)
//        }
//        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                try {
//                    if (position != 0) {
//                        mEqualizer.usePreset((position - 1).toShort())
//                        settings.presetPos = position
//                        val numberOfFreqBands: Short = 5
//                        val lowerEqualizerBandLevel = mEqualizer.bandLevelRange[0]
//                        for (i in 0 until numberOfFreqBands) {
//                            seekBarFinal[i]!!.progress =
//                                mEqualizer.getBandLevel(i.toShort()) - lowerEqualizerBandLevel
//                            points[i] =
//                                (mEqualizer.getBandLevel(i.toShort()) - lowerEqualizerBandLevel).toFloat()
//                            settings.seekbarpos[i] = mEqualizer.getBandLevel(
//                                i.toShort()
//                            ).toInt()
//                            settings.equalizerModel!!.seekbarpos[i] = mEqualizer.getBandLevel(
//                                i.toShort()
//                            ).toInt()
//                        }
//                        dataset.updateValues(points)
//                        chart.notifyDataUpdate()
//                        Toast.makeText(this@MyEqualizer, "Updating Success", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                } catch (e: Exception) {
//                    Toast.makeText(
//                        this@MyEqualizer,
//                        "Error while updating Equalizer",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                settings.equalizerModel!!.presetPos = position
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }
    }

    companion object {
        var themeColor: Int = Color.parseColor("#B24242")
        var showBackButton = true
    }
}