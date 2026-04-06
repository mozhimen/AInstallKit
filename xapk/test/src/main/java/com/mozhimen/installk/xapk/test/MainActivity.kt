package com.mozhimen.installk.xapk.test

import android.os.Bundle
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_WRITE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.utilk.java.io.isFileNotExist
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrPath
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.kotlin.isFileNotExist
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2file
import com.mozhimen.installk.xapk.test.databinding.ActivityMainBinding
import java.io.File
import com.mozhimen.installk.xapk.utils.InstallKXapkUtil
import com.mozhimen.kotlin.utilk.kotlin.strAssetName2file_use
import com.mozhimen.permissionk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsNavHostUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsRequestUtil
import com.mozhimen.uik.databinding.bases.viewbinding.activity.BaseActivityVB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivityVB<ActivityMainBinding>() {

    private val _strXApkPathName by lazy { UtilKStrPath.Absolute.Internal.getFiles() + "/" + "test.xapk" }
    override fun initView(savedInstanceState: Bundle?) {
        vb.installTv.setOnClickListener {
            checkAndInstall()
        }
    }

    private fun checkAndInstall() {
        applyPermissionInstall(this) {
            applyPermissionStorage(this) {
                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        if (_strXApkPathName.isFileNotExist()) {
                            "test.xapk".strAssetName2file_use(_strXApkPathName)
                        }
                    }
                    install(_strXApkPathName)
                }
            }
        }
    }

    @OptIn(OUsesPermission_REQUEST_INSTALL_PACKAGES::class)
    private fun applyPermissionInstall(context: Context, onGranted: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_REQUEST_INSTALL_PACKAGES(context)) {
            onGranted.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_REQUEST_INSTALL_PACKAGES(context, {
                onGranted.invoke()
            }, {
                XXPermissionsNavHostUtil.startPermission_REQUEST_INSTALL_PACKAGES(context)
            })
        }
    }

    @OptIn(OUsesPermission_READ_EXTERNAL_STORAGE::class, OUsesPermission_WRITE_EXTERNAL_STORAGE::class, OUsesPermission_MANAGE_EXTERNAL_STORAGE::class)
    @SuppressLint("MissingPermission")
    private fun applyPermissionStorage(context: Context, onGranted: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_EXTERNAL_STORAGE(context)) {
            onGranted.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_EXTERNAL_STORAGE(context, {
                onGranted.invoke()
            }, {
                XXPermissionsNavHostUtil.startPermission_EXTERNAL_STORAGE(context)
            })
        }
    }

    @OptIn(OUsesPermission_WRITE_EXTERNAL_STORAGE::class, OUsesPermission_MANAGE_EXTERNAL_STORAGE::class)
    @SuppressLint("MissingPermission")
    private fun install(strXApkPathName: String) {
        val xapkInstaller = InstallKXapkUtil.createXapkInstaller(strXApkPathName)

        if (xapkInstaller == null) {
            Toast.makeText(this, "安装xapk失败！", Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                xapkInstaller.installXapk(this@MainActivity)
            }
        }
    }
}