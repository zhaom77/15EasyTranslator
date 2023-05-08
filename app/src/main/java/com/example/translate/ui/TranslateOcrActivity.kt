package com.example.translate.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.translate.R
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.config.TranslatorConfig
import com.example.translate.databinding.ActivityTranslateOcrLayoutBinding
import com.example.translate.dialog.CameraPermissionDescDialog
import com.example.translate.dialog.LoadingDialog
import com.example.translate.info.LanguageInfo
import com.example.translate.manager.CommunicationManager
import com.example.translate.manager.FirebaseManager
import com.example.translate.manager.TranslateManager
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume

class TranslateOcrActivity : BaseActivity() {

    private val mBinding by lazy { ActivityTranslateOcrLayoutBinding.inflate(layoutInflater) }

    private var mImageCapture: ImageCapture? = null

    private var mPath = ""

    private var mIsGotoSetting = false

    private var mSelectLanInfo =
        TranslateManager.instance.getOcrLanguageInfo(TranslatorConfig.ocrLanCode)

    private var mSelectResult: ActivityResultLauncher<Intent>? = null

    private var mCameraProvider: ProcessCameraProvider? = null

    private var mPermissionDialog: CameraPermissionDescDialog? = null

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            val isSetting = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            mPermissionDialog = CameraPermissionDescDialog(this, isSetting) {
                if (isSetting) {
                    mIsGotoSetting = true
                } else {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                }
            }
            mPermissionDialog?.show()
        } else {
            startPreview()
        }
        mBinding.apply {
            backImage.setOnClickListener {
                onBackPressed()
            }
            photographImage.setOnClickListener {
                guideBgView.visibility = View.GONE
                clickTranslateText.visibility = View.GONE
                startIdentify()
            }
            formerLanguageLayout.setOnClickListener {
                mSelectResult?.launch(Intent(
                    this@TranslateOcrActivity, SelectLanguageActivity::class.java
                ).apply {
                    putExtra(
                        SelectLanguageActivity.SELECT_TYPE,
                        SelectLanguageActivity.SelectType.SELECT_OCR
                    )
                })
            }
            formerLanguageText.text = mSelectLanInfo.name
            initSelectLanInfo()
            if (TranslatorConfig.firstUseOcr) {
                TranslatorConfig.firstUseOcr = false
                guideBgView.visibility = View.VISIBLE
                clickTranslateText.visibility = View.VISIBLE
                guideBgView.setOnClickListener {
                    guideBgView.visibility = View.GONE
                    clickTranslateText.visibility = View.GONE
                    startIdentify()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (mBinding.guideBgView.visibility != View.VISIBLE) super.onBackPressed()
    }

    private fun initSelectLanInfo() {
        mSelectResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val info: LanguageInfo? =
                    it.data?.getParcelableExtra(SelectLanguageActivity.SELECT_LANGUAGE_INFO)
                if (info != null) {
                    TranslatorConfig.ocrLanCode = info.code
                    mSelectLanInfo = info
                    mBinding.formerLanguageText.text = mSelectLanInfo.name
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (mIsGotoSetting) {
            mIsGotoSetting = false
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                mPermissionDialog?.dismiss()
                startPreview()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.reject_camera_permission, Toast.LENGTH_SHORT).show()
            } else {
                startPreview()
            }
        }
    }


    private fun startPreview() {
        // 获取相机过程提供者实例
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // 为相机过程提供者添加监听
        cameraProviderFuture.addListener({

            // 获取具体的相机过程提供者
            mCameraProvider = cameraProviderFuture.get()
            // 创建相机预览窗口
            val preview = Preview.Builder().build().also {
                // 这里通过我们布局中的Preview进行预览
                it.setSurfaceProvider(mBinding.previewView.surfaceProvider)
            }

            // 获取用于拍照的实例
            mImageCapture = ImageCapture.Builder().build()

            // 指定用于预览的相机，默认为后置相机，如果需要前置相机预览请使用CameraSelector.DEFAULT_FRONT_CAMERA
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // 绑定提供者之前先进行解绑，不能重复绑定
                mCameraProvider?.unbindAll()

                // 绑定提供者,将相机的生命周期进行绑定，因为camerax具有生命周期感知力，所以消除打开和关闭相机的任务
                mCameraProvider?.bindToLifecycle(this, cameraSelector, preview, mImageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 拍照
     */
    private fun startIdentify() {
        // 校验是否有可用的相机拍摄器
        val imageCapture = mImageCapture ?: return
        val dialog = LoadingDialog(this, R.string.identifying)
        dialog.show()
        FirebaseManager.instance.onEvent(FirebaseManager.EventType.START_OCR)
        GlobalScope.launch {
            var resultText: String? = null
            val identify = async {
                resultText = takePhoto(imageCapture)
                1
            }
            var loadSuccess = false
            val loadAd = async {
                loadSuccess = loadAd()
                1
            }
            identify.await() + loadAd.await()
            withContext(Dispatchers.Main) {
                dialog.setComplete {
                    if (loadSuccess) {
                        AdManager.instance.showAd(
                            this@TranslateOcrActivity, AdPosition.INTERSTITIAL_AD
                        ) {
                            showResult(resultText)
                        }
                    } else {
                        showResult(resultText)
                    }
                }
            }
        }

    }

    private fun showResult(text: String?) {
        startPreview()
        if (text.isNullOrEmpty()) {
            FirebaseManager.instance.onEvent(FirebaseManager.EventType.OCR_FAIL)
            Toast.makeText(this, R.string.identify_fail_tip, Toast.LENGTH_SHORT).show()
        } else {
            FirebaseManager.instance.onEvent(FirebaseManager.EventType.OCR_SUCCESS)
            startActivity(Intent(this, TranslateOcrResultActivity::class.java).apply {
                putExtra(TranslateOcrResultActivity.RESULT_TEXT, text)
                putExtra(TranslateOcrResultActivity.PIC_PATH, mPath)
            })
            CommunicationManager.instance.setFinishListener(TranslateOcrActivity::class.java) {
                finish()
            }
        }
    }

    private suspend fun takePhoto(imageCapture: ImageCapture): String? =
        suspendCancellableCoroutine {
            // 定义拍摄相片名称
            mPath = "${filesDir.path}/ocr/ocr.jpg"
            val file = File(mPath)
            if (!file.exists()) {
                file.mkdirs()
            } else {
                file.delete()
            }

            // 指定输出参数
            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            // 开始拍摄相片
            imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        it.resume(null)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        // 拍摄成功，saveUri就是图片的uri地址
                        GlobalScope.launch {
                            val text = startScan(output.savedUri)
                            it.resume(text)
                        }
                    }
                })
        }

    private suspend fun startScan(uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        withContext(Dispatchers.Main) {
            mCameraProvider?.unbindAll()
        }
        return TranslateManager.instance.startOcrScan(
            this@TranslateOcrActivity, mSelectLanInfo.code, uri
        )
    }

    private suspend fun loadAd(): Boolean = suspendCancellableCoroutine {
        var outTime = false
        val job = GlobalScope.launch {
            delay(10000)
            outTime = true
            it.resume(false)
        }
        AdManager.instance.loadAd(this, AdPosition.INTERSTITIAL_AD) { success ->
            if (!outTime) {
                job.cancel()
                it.resume(success)
            }
        }
    }

}