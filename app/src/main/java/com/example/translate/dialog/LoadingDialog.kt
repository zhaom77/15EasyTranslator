package com.example.translate.dialog

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.animation.addListener
import com.example.translate.R
import com.example.translate.databinding.DialogTranslatingLayoutBinding
import com.example.translate.ex.layoutInflater
import java.util.Random

class LoadingDialog(
    context: Context,
    @StringRes private val descId: Int
) : AlertDialog(context, R.style.default_dialog) {

    private val mBinding by lazy { DialogTranslatingLayoutBinding.inflate(context.layoutInflater) }

    private var mProgressAnim: ValueAnimator? = null
    private var mCurrentProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        setCancelable(false)
        mBinding.translatingText.text = String.format(context.getString(descId), "0%")
        val num = Random().nextInt(10) + 1
        startAnim(20000, 100 - num)
    }

    fun setComplete(cb: (() -> Unit)) {
        if (mCurrentProgress >= 100) {
            dismiss()
            cb.invoke()
            return
        }
        startAnim(500, 100) {
            dismiss()
            cb.invoke()
        }
    }

    private fun startAnim(duration: Long, end: Int, onEnd: (() -> Unit)? = null) {
        mProgressAnim?.cancel()
        mProgressAnim = ValueAnimator.ofInt(mCurrentProgress, end)
        mProgressAnim?.apply {
            this.duration = duration
            addUpdateListener {
                mCurrentProgress = it.animatedValue as Int
                mBinding.translatingText.text =
                    String.format(context.getString(descId), "$mCurrentProgress%")
            }

            var isCancel = false
            addListener(onCancel = {
                isCancel = true
            }, onEnd = {
                if (!isCancel) onEnd?.invoke()
            })
            start()
        }
    }

}