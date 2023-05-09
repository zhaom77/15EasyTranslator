package com.example.translate.manager

import android.content.Context
import android.net.Uri
import com.example.translate.info.LanguageInfo
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TranslateManager {

    companion object {
        const val TAG = "TranslateManager"
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { TranslateManager() }
        const val TIME_OUT = 60 * 1000L
    }

    suspend fun startOcrScan(context: Context, sourceLan: String, uri: Uri): String? =
        suspendCancellableCoroutine {
            val image = createImage(context, uri)
            if (image == null) {
                it.resume(null)
                return@suspendCancellableCoroutine
            }
            val recognizer = getTextRecognizer(sourceLan)
            var isTimeOut = false
            val job = GlobalScope.launch {
                delay(TIME_OUT)
                isTimeOut = true
                it.resume(null)
            }
            recognizer.process(image).addOnSuccessListener { text ->
                if (!isTimeOut) {
                    job.cancel()
                    it.resume(text.text)
                }
            }.addOnFailureListener { _ ->
                if (!isTimeOut) {
                    job.cancel()
                    it.resume(null)
                }
            }
        }

    private fun getTextRecognizer(sourceLan: String): TextRecognizer {
        return when (sourceLan) {
            "zh" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            "ko" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            "ja" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            "devanagari" -> TextRecognition.getClient(
                DevanagariTextRecognizerOptions.Builder().build()
            )
            else -> TextRecognition.getClient(TextRecognizerOptions.Builder().build())
        }
    }

    private fun createImage(context: Context, uri: Uri): InputImage? {
        return try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * @return null:表示识别失败，空表示不支持语言
     */
    suspend fun getTextLan(text: String): String? = suspendCancellableCoroutine {
        var isTimeOut = false
        val job = GlobalScope.launch {
            delay(TIME_OUT)
            isTimeOut = true
            it.resume(null)
        }
        LanguageIdentification.getClient().identifyLanguage(text).addOnSuccessListener { code ->
            if (!isTimeOut) {
                job.cancel()
                if (code != "und") {
                    it.resume(code)
                } else {
                    it.resume("")
                }
            }
        }.addOnFailureListener { _ ->
            if (!isTimeOut) {
                job.cancel()
                it.resume(null)
            }
        }
    }

    suspend fun translate(text: String, sourceLan: String, targetLan: String): String? =
        suspendCancellableCoroutine { continuation ->
            val options = TranslatorOptions.Builder().setSourceLanguage(sourceLan)
                .setTargetLanguage(targetLan).build()
            val translator = Translation.getClient(options)
            var isTimeOut = false
            val job = GlobalScope.launch {
                delay(TIME_OUT)
                isTimeOut = true
                continuation.resume(null)
            }
            translator.downloadModelIfNeeded().addOnSuccessListener {
                translator.translate(text).addOnSuccessListener {
                    Logger.d({ TAG }, { "translate success" })
                    if (!isTimeOut) {
                        job.cancel()
                        continuation.resume(it)
                    }
                }.addOnFailureListener {
                    Logger.d({ TAG }, { "translate fail" })
                    if (!isTimeOut) {
                        job.cancel()
                        continuation.resume(null)
                    }
                }
            }.addOnFailureListener { _ ->
                Logger.d({ TAG }, { "download translate fail" })
                if (!isTimeOut) {
                    job.cancel()
                    continuation.resume(null)
                }
            }
        }

    fun getLanguageInfo(lanCode: String): LanguageInfo {
        for (info in mSupportLanguageList) {
            if (info.code == lanCode) {
                return info
            }
        }
        return LanguageInfo("en", "English")
    }

    fun getOcrLanguageInfo(lanCode: String): LanguageInfo {
        for (info in mOcrLanguageList) {
            if (info.code == lanCode) {
                return info
            }
        }
        return LanguageInfo("latin", "Latin")
    }


    val mSupportLanguageList = arrayListOf(
        LanguageInfo("-1", "Auto Detect"),
        LanguageInfo("en", "English"),
        LanguageInfo("es", "Spanish"),
        LanguageInfo("de", "German"),
        LanguageInfo("it", "Italian"),
        LanguageInfo("hi", "Hindi"),
        LanguageInfo("id", "Indonesian"),
        LanguageInfo("pt", "Portuguese"),
        LanguageInfo("fr", "French")
    )

    val mOcrLanguageList = arrayListOf(
        LanguageInfo("latin", "Latin"),
        LanguageInfo("zh", "Chinese"),
        LanguageInfo("ko", "Korean"),
        LanguageInfo("devanagari", "Devanagari"),
        LanguageInfo("ja", "Japanese")
    )

}