package com.example.translate.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.databinding.ActivityTranslateOcrResultLayoutBinding
import com.example.translate.ex.screenSize
import com.example.translate.manager.CommunicationManager
import com.example.translate.manager.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslateOcrResultActivity : AdActivity() {

    companion object {
        const val RESULT_TEXT = "result_text"
        const val PIC_PATH = "pic_path"
    }

    private val mBinding by lazy { ActivityTranslateOcrResultLayoutBinding.inflate(layoutInflater) }

    private var mBitmap: Bitmap? = null

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = intent.getStringExtra(RESULT_TEXT) ?: ""
        Logger.d({ "ocrResult" }, { "text: $text" })
        val picPath = intent.getStringExtra(PIC_PATH)
        mBinding.apply {
            ocrResultText.text = text
            backImage.setOnClickListener {
                onBackPressed()
            }
            copyButton.setOnClickListener {
                finish()
                startActivity(Intent(
                    this@TranslateOcrResultActivity, TranslateActivity::class.java
                ).apply {
                    putExtra(TranslateActivity.TRANSLATE_TEXT, text)
                    putExtra(TranslateActivity.START_AUTO_TRANSLATE, true)
                })
                CommunicationManager.instance.startFinish(TranslateOcrActivity::class.java)
            }
            retryButton.setOnClickListener {
                finish()
            }
            ocrResultText.movementMethod = ScrollingMovementMethod.getInstance()
        }
        setPic(picPath)
    }

    override fun onResume() {
        super.onResume()
        AdManager.instance.loadAd(this, AdPosition.INTERSTITIAL_AD)
    }

    private fun setPic(path: String?) {
        if (path.isNullOrEmpty()) return
        GlobalScope.launch {
            mBitmap = createPic(path)
            withContext(Dispatchers.Main) {
                mBinding.picImage.setImageBitmap(mBitmap)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBitmap?.recycle()
        mBitmap = null
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    private fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return degree
    }


    /**
     * 生成图片
     */
    private fun createPic(path: String): Bitmap {
        val angle = readPictureDegree(path)
        val bitmap = BitmapFactory.decodeFile(path)
        if (angle == 0) {
            return bitmap
        }
        Logger.d({ "ocrResult" }, { "screenW: ${screenSize.x} screenH: ${screenSize.y}" })
        Logger.d({ "ocrResult" }, { "w: ${bitmap.width} h: ${bitmap.height}" })
        //旋转图片 动作
        val matrix = Matrix()
        matrix.setRotate(angle.toFloat(), bitmap.width / 2f, bitmap.height / 2f)
        // 创建新的图片
        val bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        Logger.d({ "ocrResult" }, { "bitmap1 w: ${bitmap1.width} h: ${bitmap1.height}" })
        bitmap.recycle()
        return bitmap1
    }

}