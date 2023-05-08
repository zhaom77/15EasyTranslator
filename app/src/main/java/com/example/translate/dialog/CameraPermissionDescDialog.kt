package com.example.translate.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.translate.R
import com.example.translate.databinding.DialogCameraPermissionDescLayoutBinding
import com.example.translate.ex.layoutInflater

class CameraPermissionDescDialog(
    context: Context,
    private val isSetting: Boolean,
    private val listener: () -> Unit
) : AlertDialog(context, R.style.default_dialog) {

    private val mBinding by lazy { DialogCameraPermissionDescLayoutBinding.inflate(context.layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.apply {
            closeButton.setOnClickListener {
                dismiss()
            }
            openButton.setOnClickListener {
                if (isSetting) {
                    listener.invoke()
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } else {
                    dismiss()
                    listener.invoke()
                }
            }
        }
        setCancelable(false)
    }

}