package com.bihe0832.android.common.crop.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.bihe0832.android.common.crop.CropUtils
import com.bihe0832.android.common.crop.R
import com.bihe0832.android.common.crop.callback.BitmapCropCallback
import com.bihe0832.android.common.crop.constants.CropConstants
import com.bihe0832.android.common.crop.model.AspectRatio
import com.bihe0832.android.common.crop.view.CropImageView
import com.bihe0832.android.common.crop.view.CropView
import com.bihe0832.android.common.crop.view.GestureCropImageView
import com.bihe0832.android.common.crop.view.OverlayView
import com.bihe0832.android.common.crop.view.TransformImageView
import com.bihe0832.android.common.crop.view.widget.AspectRatioTextView
import com.bihe0832.android.common.crop.view.widget.HorizontalProgressWheelView
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.bottom.bar.BottomBar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2023/11/15.
 * Description:
 *
 */
class CropActivity : BaseActivity() {
    private val TABS_COUNT = 3
    private val SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000
    private val ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42

    protected var mBottomBar: BottomBar? = null
    private var mCropView: CropView? = null
    private var mGestureCropImageView: GestureCropImageView? = null
    private var mOverlayView: OverlayView? = null
    private var mBlockingView: View? = null

    private var showAspectTab = true
    private var showRotateTab = true
    private var showScaleTab = true

    private var mCompressFormat = CropConstants.DEFAULT_COMPRESS_FORMAT
    private var mCompressQuality = CropConstants.DEFAULT_COMPRESS_QUALITY
    private val mCropAspectRatioViews = mutableListOf<ViewGroup>()

    private var mAllowedGestures = ConcurrentHashMap<Int, Int>().apply {
        put(TAB_ID_ASPECT, CropConstants.GESTURE_TYPES_SCALE)
        put(TAB_ID_ROTATE, CropConstants.GESTURE_TYPES_ROTATE)
        put(TAB_ID_SCALE, CropConstants.GESTURE_TYPES_SCALE)
    }

    private val mImageListener: TransformImageView.TransformImageListener = object :
        TransformImageView.TransformImageListener {
        override fun onRotate(currentAngle: Float) {
            setAngleText(currentAngle)
        }

        override fun onScale(currentScale: Float) {
            setScaleText(currentScale)
        }

        override fun onLoadComplete() {
            mCropView?.animate()?.alpha(1f)?.setDuration(300)?.interpolator = AccelerateInterpolator()
            mBlockingView?.isClickable = false
        }

        override fun onLoadFailure(e: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.com_bihe_0832_android_common_crop_activity)
        initMenu()
        parseBundle(intent)
        initiateRootViews(intent)
        setupAspectRatioWidget(intent)
        setupRotateWidget()
        setupScaleWidget()
        initTab()
        addBlockingView()
        setImageData(intent)
    }

    private fun initMenu() {
        findViewById<View>(R.id.crop_back).setOnClickListener {
            onBack()
        }
        findViewById<View>(R.id.crop_done).setOnClickListener {
            cropAndSaveImage()
        }
    }

    override fun getStatusBarColor(): Int {
        return resources.getColor(R.color.crop_color_default_dimmed)
    }

    override fun onBack() {
        setResult(RESULT_CANCELED, Intent())
        finish()
    }

    private fun cropAndSaveImage() {
        mBlockingView!!.isClickable = true
        supportInvalidateOptionsMenu()
        mGestureCropImageView!!.cropAndSaveImage(
            mCompressFormat,
            mCompressQuality,
            object : BitmapCropCallback {
                override fun onBitmapCropped(
                    resultUri: Uri,
                    offsetX: Int,
                    offsetY: Int,
                    imageWidth: Int,
                    imageHeight: Int,
                ) {
                    setResultUri(
                        resultUri,
                        mGestureCropImageView!!.targetAspectRatio,
                        offsetX,
                        offsetY,
                        imageWidth,
                        imageHeight,
                    )
                    finish()
                }

                override fun onCropFailure(t: Throwable) {
                    t.printStackTrace()
                    ZLog.e(t.toString())
                    onBack()
                }
            },
        )
    }

    private fun setResultUri(
        uri: Uri?,
        resultAspectRatio: Float,
        offsetX: Int,
        offsetY: Int,
        imageWidth: Int,
        imageHeight: Int,
    ) {
        setResult(
            RESULT_OK,
            Intent().apply {
                setData(uri)
                putExtra(CropUtils.EXTRA_OUTPUT_URI, uri)
                putExtra(CropUtils.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                putExtra(CropUtils.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                putExtra(CropUtils.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                putExtra(CropUtils.EXTRA_OUTPUT_OFFSET_X, offsetX)
                putExtra(CropUtils.EXTRA_OUTPUT_OFFSET_Y, offsetY)
            },
        )
    }

    private fun initTab() {
        if (showAspectTab || showRotateTab || showScaleTab) {
            mBottomBar = findViewById<View>(R.id.crop_controls) as BottomBar
            mBottomBar?.apply {
                if (showAspectTab) {
                    addItem(
                        CropBottomTab(
                            context,
                            R.drawable.ic_crop,
                            ThemeResourcesManager.getString(ModelResR.string.menu_crop),
                            TAB_ID_ASPECT,
                        ),
                    )
                }

                if (showRotateTab) {
                    addItem(
                        CropBottomTab(
                            context,
                            R.drawable.ic_rotate,
                            ThemeResourcesManager.getString(ModelResR.string.menu_rotate),
                            TAB_ID_ROTATE,
                        ),
                    )
                }

                if (showScaleTab) {
                    addItem(
                        CropBottomTab(
                            context,
                            R.drawable.ic_scale,
                            ThemeResourcesManager.getString(ModelResR.string.menu_scale),
                            TAB_ID_SCALE,
                        ),
                    )
                }

                setOnTabSelectedListener(object : BottomBar.OnTabSelectedListener {

                    override fun onTabSelected(position: Int, prePosition: Int) {
                        updateControl(prePosition, false)
                        updateControl(position, true)
                    }

                    override fun onTabUnselected(position: Int) {
                    }

                    override fun onTabReselected(position: Int) {
                        updateControl(position, true)
                    }
                })
                setCurrentItem(0)
            }
        } else {
            findViewById<View>(R.id.wrapper_controls)?.visibility = View.GONE
            setAllowedGestures(TAB_ID_ASPECT)
        }
    }

    private fun updateControl(position: Int, selected: Boolean) {
        val scale_scroll_wheel = findViewById<View>(R.id.scale_scroll_wheel)
        val rotate_scroll_wheel = findViewById<View>(R.id.rotate_scroll_wheel)
        val angel_value = findViewById<TextView>(R.id.angel_value)
        val layout_aspect_ratio = findViewById<View>(R.id.layout_aspect_ratio)
        val angel_reset = findViewById<View>(R.id.angel_reset)
        val angel_angel = findViewById<View>(R.id.angel_angel)

        (mBottomBar?.getItem(position) as CropBottomTab?)?.let {
            if (it.getTabID() == TAB_ID_SCALE) {
                changeScaleScroll(scale_scroll_wheel, angel_value, selected)
            } else if (it.getTabID() == TAB_ID_ROTATE) {
                changeRotateScroll(rotate_scroll_wheel, angel_value, angel_reset, angel_angel, selected)
            } else {
                changeAspect(layout_aspect_ratio, selected)
            }

            if (selected) {
                setAllowedGestures(it.getTabID())
            }
        }
    }

    private fun setAllowedGestures(tabID: Int) {
        mGestureCropImageView!!.isScaleEnabled = (
                mAllowedGestures[tabID] == CropConstants.GESTURE_TYPES_ALL ||
                        mAllowedGestures[tabID] == CropConstants.GESTURE_TYPES_SCALE
                )
        mGestureCropImageView!!.isRotateEnabled = (
                mAllowedGestures[tabID] == CropConstants.GESTURE_TYPES_ALL ||
                        mAllowedGestures[tabID] == CropConstants.GESTURE_TYPES_ROTATE
                )
    }

    private fun changeAspect(layout_aspect_ratio: View, selected: Boolean) {
        if (selected) {
            layout_aspect_ratio.visibility = View.VISIBLE
        } else {
            layout_aspect_ratio.visibility = View.INVISIBLE
        }
    }

    private fun changeRotateScroll(
        rotate_scroll_wheel: View,
        angel_value: TextView,
        angel_reset: View,
        angel_angel: View,
        selected: Boolean,
    ) {
        if (selected) {
            rotate_scroll_wheel.visibility = View.VISIBLE
            angel_value.visibility = View.VISIBLE
            setAngleText(mGestureCropImageView!!.currentAngle)
            angel_reset.visibility = View.VISIBLE
            angel_angel.visibility = View.VISIBLE
        } else {
            rotate_scroll_wheel.visibility = View.GONE
            angel_value.visibility = View.INVISIBLE
            angel_reset.visibility = View.INVISIBLE
            angel_angel.visibility = View.INVISIBLE
        }
    }

    private fun changeScaleScroll(
        scale_scroll_wheel: View,
        angel_value: View,
        selected: Boolean,
    ) {
        if (selected) {
            scale_scroll_wheel.visibility = View.VISIBLE
            angel_value.visibility = View.VISIBLE
            setScaleText(mGestureCropImageView!!.currentScale)
        } else {
            scale_scroll_wheel.visibility = View.GONE
            angel_value.visibility = View.INVISIBLE
        }
    }

    /**
     * This method extracts all data from the incoming intent and setups views properly.
     */
    private fun setImageData(intent: Intent) {
        val inputUri = intent.getParcelableExtra<Uri>(CropUtils.EXTRA_INPUT_URI)
        if (inputUri != null) {
            try {
                mGestureCropImageView!!.setImageUri(inputUri)
            } catch (e: java.lang.Exception) {
                finish()
            }
        } else {
            finish()
        }
    }

    fun parseBundle(bundle: Intent) {
        val compressionFormatName = bundle.getStringExtra(CropUtils.Options.EXTRA_COMPRESSION_FORMAT_NAME)
        var compressFormat: Bitmap.CompressFormat? = null
        if (!TextUtils.isEmpty(compressionFormatName)) {
            compressFormat = Bitmap.CompressFormat.valueOf(compressionFormatName!!)
        }
        mCompressFormat = compressFormat ?: CropConstants.DEFAULT_COMPRESS_FORMAT
        mCompressQuality =
            bundle.getIntExtra(CropUtils.Options.EXTRA_COMPRESSION_QUALITY, CropConstants.DEFAULT_COMPRESS_QUALITY)

        val allowedGestures = bundle.getIntArrayExtra(CropUtils.Options.EXTRA_ALLOWED_GESTURES)
        if (allowedGestures != null && allowedGestures.size == TABS_COUNT) {
            mAllowedGestures[TAB_ID_ASPECT] = allowedGestures[0]
            mAllowedGestures[TAB_ID_ROTATE] = allowedGestures[1]
            mAllowedGestures[TAB_ID_SCALE] = allowedGestures[2]
        }
        val hideTab = bundle.getBooleanExtra(CropUtils.Options.EXTRA_HIDE_BOTTOM_CONTROLS, false)
        if (hideTab) {
            showAspectTab = false
            showRotateTab = false
            showScaleTab = false
        }

        ConstraintSet().apply {
            clone(findViewById<ConstraintLayout>(R.id.crop_page))
            clear(R.id.crop_toolbar, ConstraintSet.BOTTOM)
            clear(R.id.crop_toolbar, ConstraintSet.TOP)
            clear(R.id.ucrop, ConstraintSet.BOTTOM)
            clear(R.id.ucrop, ConstraintSet.TOP)
            if (hideTab) {
                connect(
                    R.id.crop_toolbar,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                connect(
                    R.id.ucrop,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                connect(
                    R.id.ucrop,
                    ConstraintSet.BOTTOM,
                    R.id.crop_toolbar,
                    ConstraintSet.TOP,
                    0
                )
            } else {
                connect(
                    R.id.crop_toolbar,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                connect(
                    R.id.ucrop,
                    ConstraintSet.BOTTOM,
                    R.id.wrapper_controls,
                    ConstraintSet.TOP,
                )

                connect(
                    R.id.ucrop,
                    ConstraintSet.TOP,
                    R.id.crop_toolbar,
                    ConstraintSet.BOTTOM,
                    0
                )
            }
        }.applyTo(findViewById(R.id.crop_page))
    }

    private fun initiateRootViews(intent: Intent) {
        mCropView = findViewById(R.id.ucrop)
        mOverlayView = mCropView!!.overlayView

        mOverlayView!!.setDimmedColor(resources.getColor(R.color.crop_color_default_dimmed))
        mOverlayView!!.setCircleDimmedLayer(
            intent.getBooleanExtra(
                CropUtils.Options.EXTRA_CIRCLE_DIMMED_LAYER,
                OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER,
            ),
        )

        mOverlayView!!.setShowCropFrame(
            intent.getBooleanExtra(CropUtils.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME),
        )
        mOverlayView!!.setCropFrameColor(resources.getColor(R.color.crop_default_crop_frame))
        mOverlayView!!.setCropFrameStrokeWidth(
            intent.getIntExtra(
                CropUtils.Options.EXTRA_CROP_FRAME_STROKE_WIDTH,
                resources.getDimensionPixelSize(R.dimen.crop_default_crop_frame_stoke_width),
            ),
        )

        mOverlayView!!.setShowCropGrid(
            intent.getBooleanExtra(CropUtils.Options.EXTRA_SHOW_CROP_GRID, OverlayView.DEFAULT_SHOW_CROP_GRID),
        )
        mOverlayView!!.setCropGridRowCount(
            intent.getIntExtra(CropUtils.Options.EXTRA_CROP_GRID_ROW_COUNT, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT),
        )
        mOverlayView!!.setCropGridColumnCount(
            intent.getIntExtra(
                CropUtils.Options.EXTRA_CROP_GRID_COLUMN_COUNT,
                OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT,
            ),
        )
        mOverlayView!!.setCropGridColor(resources.getColor(R.color.crop_default_crop_grid))
        mOverlayView!!.setCropGridCornerColor(resources.getColor(R.color.crop_default_crop_grid))
        mOverlayView!!.setCropGridStrokeWidth(resources.getDimensionPixelSize(R.dimen.crop_default_crop_grid_stoke_width))

        mGestureCropImageView = mCropView!!.cropImageView
        mGestureCropImageView!!.setTransformImageListener(mImageListener)
        mGestureCropImageView!!.maxBitmapSize =
            intent.getIntExtra(CropUtils.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE)
        mGestureCropImageView!!.setMaxScaleMultiplier(
            intent.getFloatExtra(
                CropUtils.Options.EXTRA_MAX_SCALE_MULTIPLIER,
                CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER,
            ),
        )
        mGestureCropImageView!!.setImageToWrapCropBoundsAnimDuration(
            CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong(),
        )

        val aspectRatioX = intent.getFloatExtra(CropUtils.Options.EXTRA_ASPECT_RATIO_X, -1f)
        val aspectRatioY = intent.getFloatExtra(CropUtils.Options.EXTRA_ASPECT_RATIO_Y, -1f)

        val aspectRationSelectedByDefault =
            intent.getIntExtra(CropUtils.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0)
        val aspectRatioList = intent.getParcelableArrayListExtra<AspectRatio>(
            CropUtils.Options.EXTRA_ASPECT_RATIO_OPTIONS,
        )

        if (aspectRatioX >= 0 && aspectRatioY >= 0) {
            showAspectTab = false
            val targetAspectRatio = aspectRatioX / aspectRatioY
            mGestureCropImageView!!.targetAspectRatio = if (java.lang.Float.isNaN(targetAspectRatio)) {
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO
            } else {
                targetAspectRatio
            }
        } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size) {
            val targetAspectRatio =
                aspectRatioList[aspectRationSelectedByDefault].aspectRatioX / aspectRatioList[aspectRationSelectedByDefault].aspectRatioY
            mGestureCropImageView!!.targetAspectRatio =
                if (java.lang.Float.isNaN(targetAspectRatio)) {
                    CropImageView.SOURCE_IMAGE_ASPECT_RATIO
                } else {
                    targetAspectRatio
                }
        } else {
            mGestureCropImageView!!.targetAspectRatio = CropImageView.SOURCE_IMAGE_ASPECT_RATIO
        }

        // Result bitmap max size options
        val maxSizeX = intent.getIntExtra(CropUtils.Options.EXTRA_MAX_SIZE_X, 0)
        val maxSizeY = intent.getIntExtra(CropUtils.Options.EXTRA_MAX_SIZE_Y, 0)

        if (maxSizeX > 0 && maxSizeY > 0) {
            mGestureCropImageView!!.setMaxResultImageSizeX(maxSizeX)
            mGestureCropImageView!!.setMaxResultImageSizeY(maxSizeY)
        }

        intent.getIntExtra(CropUtils.Options.EXTRA_FREE_STYLE_CROP, OverlayView.DEFAULT_FREESTYLE_CROP_MODE).let {
            mOverlayView!!.setFreestyleCropMode(it)
            mGestureCropImageView!!.setFreestyleCropMode(it)
        }
    }

    private fun setAngleText(angle: Float) {
        findViewById<TextView>(R.id.angel_value)?.text = String.format(Locale.getDefault(), "%.1f°", angle)
    }

    private fun setScaleText(scale: Float) {
        findViewById<TextView>(R.id.angel_value)?.text =
            String.format(Locale.getDefault(), "%d%%", (scale * 100).toInt())
    }

    private fun resetRotation() {
        mGestureCropImageView!!.postRotate(-mGestureCropImageView!!.currentAngle)
        mGestureCropImageView!!.setImageToWrapCropBounds()
    }

    private fun rotateByAngle(angle: Int) {
        mGestureCropImageView!!.postRotate(angle.toFloat())
        mGestureCropImageView!!.setImageToWrapCropBounds()
    }

    private fun setupRotateWidget() {
        (findViewById<View>(R.id.rotate_scroll_wheel) as HorizontalProgressWheelView)
                .setScrollingListener(object : HorizontalProgressWheelView.ScrollingListener {
                    override fun onScroll(delta: Float, totalDistance: Float) {
                        mGestureCropImageView!!.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT)
                    }

                    override fun onScrollEnd() {
                        mGestureCropImageView!!.setImageToWrapCropBounds()
                    }

                    override fun onScrollStart() {
                        mGestureCropImageView!!.cancelAllAnimations()
                    }
                })
        findViewById<View>(R.id.angel_reset).setOnClickListener { resetRotation() }
        findViewById<View>(R.id.angel_angel).setOnClickListener { rotateByAngle(90) }
    }

    private fun setupAspectRatioWidget(bundle: Intent) {
        var aspectRationSelectedByDefault =
            bundle.getIntExtra(CropUtils.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0)
        var aspectRatioList = bundle.getParcelableArrayListExtra<AspectRatio?>(
            CropUtils.Options.EXTRA_ASPECT_RATIO_OPTIONS,
        )
        if (aspectRatioList == null || aspectRatioList.isEmpty()) {
            aspectRationSelectedByDefault = 2
            aspectRatioList = ArrayList()
            aspectRatioList.add(AspectRatio(null, 1f, 1f))
            aspectRatioList.add(AspectRatio(null, 3f, 4f))
            aspectRatioList.add(
                AspectRatio(
                    getString(ModelResR.string.label_original).uppercase(Locale.getDefault()),
                    CropImageView.SOURCE_IMAGE_ASPECT_RATIO,
                    CropImageView.SOURCE_IMAGE_ASPECT_RATIO,
                ),
            )
            aspectRatioList.add(AspectRatio(null, 3f, 2f))
            aspectRatioList.add(AspectRatio(null, 16f, 9f))
        }
        val wrapperAspectRatioList = findViewById<LinearLayout>(R.id.layout_aspect_ratio)
        var wrapperAspectRatio: FrameLayout
        var aspectRatioTextView: AspectRatioTextView
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        lp.weight = 1f
        for (aspectRatio in aspectRatioList) {
            wrapperAspectRatio =
                layoutInflater.inflate(R.layout.com_bihe_0832_android_common_crop_aspect_ratio, null) as FrameLayout
            wrapperAspectRatio.layoutParams = lp
            aspectRatioTextView = wrapperAspectRatio.getChildAt(0) as AspectRatioTextView
            aspectRatioTextView.setActiveColor(ThemeResourcesManager.getColor(ResR.color.colorAccent) ?: Color.BLACK)
            aspectRatioTextView.setAspectRatio(aspectRatio!!)
            wrapperAspectRatioList.addView(wrapperAspectRatio)
            mCropAspectRatioViews.add(wrapperAspectRatio)
        }
        mCropAspectRatioViews.get(aspectRationSelectedByDefault).isSelected = true
        for (cropAspectRatioView in mCropAspectRatioViews) {
            cropAspectRatioView.setOnClickListener { v ->
                mGestureCropImageView!!.targetAspectRatio =
                    ((v as ViewGroup).getChildAt(0) as AspectRatioTextView).getAspectRatio(v.isSelected())
                mGestureCropImageView!!.setImageToWrapCropBounds()
                if (!v.isSelected()) {
                    for (cropAspectRatioView in mCropAspectRatioViews) {
                        cropAspectRatioView.isSelected = cropAspectRatioView === v
                    }
                }
            }
        }
    }

    private fun setupScaleWidget() {
        (findViewById<View>(R.id.scale_scroll_wheel) as HorizontalProgressWheelView)
                .setScrollingListener(object : HorizontalProgressWheelView.ScrollingListener {
                    override fun onScroll(delta: Float, totalDistance: Float) {
                        if (delta > 0) {
                            mGestureCropImageView!!.zoomInImage(
                                mGestureCropImageView!!.currentScale +
                                        delta * (
                                        (mGestureCropImageView!!.maxScale - mGestureCropImageView!!.minScale) /
                                                SCALE_WIDGET_SENSITIVITY_COEFFICIENT
                                        ),
                            )
                        } else {
                            mGestureCropImageView!!.zoomOutImage(
                                (
                                        mGestureCropImageView!!.currentScale +
                                                delta * (
                                                (mGestureCropImageView!!.maxScale - mGestureCropImageView!!.minScale) /
                                                        SCALE_WIDGET_SENSITIVITY_COEFFICIENT
                                                )
                                        ),
                            )
                        }
                    }

                    override fun onScrollEnd() {
                        mGestureCropImageView!!.setImageToWrapCropBounds()
                    }

                    override fun onScrollStart() {
                        mGestureCropImageView!!.cancelAllAnimations()
                    }
                })
    }

    private fun addBlockingView() {
        if (mBlockingView == null) {
            mBlockingView = View(this)
            val lp = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            mBlockingView!!.layoutParams = lp
            mBlockingView!!.isClickable = true
        }
        (findViewById<View>(R.id.crop_page) as ConstraintLayout).addView(mBlockingView)
    }
}
