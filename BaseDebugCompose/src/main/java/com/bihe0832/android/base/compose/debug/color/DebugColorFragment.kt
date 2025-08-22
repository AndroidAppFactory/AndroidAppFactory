package com.bihe0832.android.base.compose.debug.color

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.lib.color.picker.OnAlphaSelectedListener
import com.bihe0832.android.lib.color.picker.OnColorSelectedListener
import com.bihe0832.android.lib.color.picker.alpha.AlphaSlideView
import com.bihe0832.android.lib.color.picker.color.ColorRingPickerView
import com.bihe0832.android.lib.color.picker.color.ColorWheelPickerView
import com.bihe0832.android.lib.color.picker.deep.DeepSlideView
import com.bihe0832.android.lib.color.picker.dialog.ColorDialogUtils
import com.bihe0832.android.lib.color.utils.ColorUtils
import com.bihe0832.android.lib.log.ZLog


class DebugColorFragment : Fragment() {

    private val TAG = "DebugColorFragment"

    private var defaultColor = Color.WHITE
    private var defaultAlpha = 255

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_test_color, null)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    fun initView(view: View) {
        view.findViewById<View>(R.id.show_color_dialog).setOnClickListener {
            val color = Color.parseColor("#23ddff")
            ZLog.d(TAG, "color:$color")
            ZLog.d(TAG, "color:" + ColorUtils.color2Hex(color))
            ZLog.d(TAG, "color:" + ColorUtils.getColorBrightness(color))
            ColorUtils.addAlpha(50, color).let { alphaColor ->
                ZLog.d(TAG, "color alphaColor:$alphaColor")
                ZLog.d(TAG, "color alphaColor:" + ColorUtils.color2Hex(alphaColor, true))
                ZLog.d(TAG, "color:" + ColorUtils.getColorBrightness(alphaColor))

                ColorUtils.removeAlpha(alphaColor).let { removeColor ->
                    ZLog.d(TAG, "color removeColor:$removeColor")
                    ZLog.d(TAG, "color removeColor:" + ColorUtils.color2Hex(removeColor))
                    ZLog.d(TAG, "color:" + ColorUtils.getColorBrightness(removeColor))
                    ZLog.d(
                        TAG,
                        "color removeColor:${Color.parseColor(ColorUtils.color2Hex(removeColor))}"
                    )
                }
            }

            ColorDialogUtils.showColorSelectDialog(
                context,
                defaultAlpha,
                defaultColor,
            ) { result ->
                defaultAlpha = ColorUtils.getAlpha(result)
                defaultColor = result
                changeColor()
            }
        }
        view.findViewById<ColorWheelPickerView>(R.id.dialog_color_wheel_view)
            .setOnColorSelectedListener(object : OnColorSelectedListener {
                override fun onColorSelecting(color: Int) {
                    defaultColor = color
                    changeColor()
                }

                override fun onColorSelected(color: Int) {
                    defaultColor = color
                    changeColor()
                }
            })
        view.findViewById<ColorRingPickerView>(R.id.color_ring_view).apply {
            hasScaleMirror = true
            setSelectedBlockColor(defaultColor)
            setOnColorSelectedListener(object : OnColorSelectedListener {
                override fun onColorSelecting(color: Int) {
                    defaultColor = color
                    changeColor()
                }

                override fun onColorSelected(color: Int) {
                    defaultColor = color
                    changeColor()
                }
            })
        }

        view.findViewById<DeepSlideView>(R.id.color_slide_view)
            .setOnColorSelectedListener(object : OnColorSelectedListener {
                override fun onColorSelecting(color: Int) {
                    defaultColor = color
                    changeColor()
                }

                override fun onColorSelected(color: Int) {
                    defaultColor = color
                    changeColor()
                }
            })
        view.findViewById<AlphaSlideView>(R.id.dialog_color_alpha_slide_view).apply {
            setBaseColor(defaultColor)
            setOnAlphaSelectedListener(object : OnAlphaSelectedListener {
                override fun onAlphaSelecting(alpha: Float) {
                    defaultAlpha = (alpha * 255).toInt()
                    changeColor()
                }

                override fun onAlphaSelected(alpha: Float) {
                    defaultAlpha = (alpha * 255).toInt()
                    changeColor()
                }
            })
        }
    }

    private fun changeColor() {
        view?.findViewById<ImageView>(R.id.iv_color)
            ?.setBackgroundColor(ColorUtils.addAlpha(defaultAlpha, defaultColor))
        view?.findViewById<AlphaSlideView>(R.id.dialog_color_alpha_slide_view)?.apply {
            setBaseColor(defaultColor)
            setBaseAlpha(defaultAlpha)
        }
        view?.findViewById<DeepSlideView>(R.id.color_slide_view)?.setBaseColor(defaultColor)
        view?.findViewById<TextView>(R.id.tv_color)?.text = "HEX: ${
            ColorUtils.color2Hex(
                defaultAlpha,
                defaultColor,
            )
        }  A:$defaultAlpha  R:${defaultColor.red}  G:${defaultColor.green}  B:${defaultColor.blue}"
    }
}
