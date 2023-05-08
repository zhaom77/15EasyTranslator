package com.example.translate.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.content.ContextCompat
import com.example.translate.R
import com.example.translate.databinding.ActivityConnectGuideLayoutBinding
import com.example.translate.manager.FirebaseManager

class ConnectGuideActivity : BaseActivity() {

    private val mBinding by lazy { ActivityConnectGuideLayoutBinding.inflate(layoutInflater) }

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseManager.instance.onEvent(FirebaseManager.EventType.VG_SHOW)
        mBinding.apply {
            skipText.setOnClickListener {
                finish()
            }
            skipText.text = String.format(getString(R.string.wait_skip), "5s")
            skipText.isEnabled = false
            val countDownTimer = object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    skipText.text = String.format(
                        getString(R.string.wait_skip),
                        "${millisUntilFinished / 1000}s"
                    )
                }

                override fun onFinish() {
                    skipText.text = getString(R.string.skip)
                    skipText.isEnabled = true
                    skipText.setBackgroundResource(R.drawable.skip_button_normal)
                    skipText.setTextColor(
                        ContextCompat.getColor(
                            this@ConnectGuideActivity,
                            R.color.color_87B6B7
                        )
                    )
                }

            }
            countDownTimer.start()

            tryItNowButton.setOnClickListener {
                startActivity(Intent(this@ConnectGuideActivity, ConnectActivity::class.java).apply {
                    putExtra(ConnectActivity.IS_AUTO_CONNECT, true)
                })
                finish()
                FirebaseManager.instance.onEvent(FirebaseManager.EventType.VG_CLK)
            }
        }
    }

    override fun onBackPressed() {

    }
}