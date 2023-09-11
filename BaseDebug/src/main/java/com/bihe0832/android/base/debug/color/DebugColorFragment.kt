package com.bihe0832.android.base.debug.color

import android.graphics.Color
import android.view.View
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.color.picker.OnAlphaSelectedListener
import com.bihe0832.android.lib.color.picker.OnColorSelectedListener
import com.bihe0832.android.lib.color.picker.dialog.ColorDialogUtils
import com.bihe0832.android.lib.color.utils.ColorUtils
import com.bihe0832.android.lib.log.ZLog
import kotlinx.android.synthetic.main.fragment_test_color.color_ring_view
import kotlinx.android.synthetic.main.fragment_test_color.color_slide_view
import kotlinx.android.synthetic.main.fragment_test_color.dialog_color_alpha_slide_view
import kotlinx.android.synthetic.main.fragment_test_color.dialog_color_wheel_view
import kotlinx.android.synthetic.main.fragment_test_color.iv_color
import kotlinx.android.synthetic.main.fragment_test_color.show_color_dialog
import kotlinx.android.synthetic.main.fragment_test_color.tv_color

class DebugColorFragment : BaseFragment() {

    private val TAG = "DebugColorFragment"
    override fun getLayoutID(): Int {
        return R.layout.fragment_test_color
    }

    private var defaultColor = Color.WHITE
    private var defaultAlpha = 255

    override fun initView(view: View) {
        super.initView(view)
        show_color_dialog.setOnClickListener {
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
                    ZLog.d(TAG, "color removeColor:${Color.parseColor(ColorUtils.color2Hex(removeColor))}")
                }
            }

            ColorDialogUtils.showColorSelectDialog(
                context!!,
                defaultAlpha,
                defaultColor,
            ) { result ->
                defaultAlpha = ColorUtils.getAlpha(result)
                defaultColor = result
                changeColor()
            }
        }
        color_ring_view.hasScaleMirror = true
        dialog_color_wheel_view.setOnColorSelectedListener(object :
            OnColorSelectedListener {
            override fun onColorSelecting(color: Int) {
                defaultColor = color
                changeColor()
            }

            override fun onColorSelected(color: Int) {
                defaultColor = color
                changeColor()
            }
        })
        color_ring_view.setSelectedBlockColor(defaultColor)
        color_ring_view.setOnColorSelectedListener(object :
            OnColorSelectedListener {
            override fun onColorSelecting(color: Int) {
                defaultColor = color
                changeColor()
            }

            override fun onColorSelected(color: Int) {
                defaultColor = color
                changeColor()
            }
        })
        color_slide_view.setOnColorSelectedListener(object :
            OnColorSelectedListener {
            override fun onColorSelecting(color: Int) {
                defaultColor = color
                changeColor()
            }

            override fun onColorSelected(color: Int) {
                defaultColor = color
                changeColor()
            }
        })
        dialog_color_alpha_slide_view.setBaseColor(defaultColor)
        dialog_color_alpha_slide_view.setOnAlphaSelectedListener(object :
            OnAlphaSelectedListener {
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

    private fun changeColor() {
        iv_color.setBackgroundColor(ColorUtils.addAlpha(defaultAlpha, defaultColor))
        dialog_color_alpha_slide_view.setBaseColor(defaultColor)
        dialog_color_alpha_slide_view.setBaseAlpha(defaultAlpha)
        color_slide_view.setBaseColor(defaultColor)
        tv_color.text =
            "HEX: ${
                ColorUtils.color2Hex(
                    defaultAlpha,
                    defaultColor,
                )
            }  A:$defaultAlpha  R:${defaultColor.red}  G:${defaultColor.green}  B:${defaultColor.blue}"
    }
}
