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
import com.mozhimen.bindk.bases.activity.viewbinding.BaseActivityVB
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_WRITE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.utilk.java.io.isFileNotExist
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrPath
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.kotlin.isFileNotExist
import com.mozhimen.kotlin.utilk.kotlin.strAssetName2file
import com.mozhimen.kotlin.utilk.kotlin.strAssetName2strFilePathName
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2file
import com.mozhimen.installk.xapk.test.databinding.ActivityMainBinding
import java.io.File
import com.mozhimen.installk.xapk.utils.InstallKXapkUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsNavHostUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil
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
                            "test.xapk".strAssetName2file(_strXApkPathName)
                        }
                    }
                    install(_strXApkPathName)
                }
            }
        }
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    private fun applyPermissionInstall(context: Context, onGranted: I_Listener) {
        if (XXPermissionsCheckUtil.hasInstallPermission(context)) {
            onGranted.invoke()
        } else {
            XXPermissionsRequestUtil.requestInstallPermission(context, {
                onGranted.invoke()
            }, {
                XXPermissionsNavHostUtil.startSettingInstall(context)
            })
        }
    }

    @OptIn(OPermission_READ_EXTERNAL_STORAGE::class, OPermission_WRITE_EXTERNAL_STORAGE::class, OPermission_MANAGE_EXTERNAL_STORAGE::class)
    @SuppressLint("MissingPermission")
    private fun applyPermissionStorage(context: Context, onGranted: I_Listener) {
        if (XXPermissionsCheckUtil.hasReadWritePermission(context)) {
            onGranted.invoke()
        } else {
            XXPermissionsRequestUtil.requestReadWritePermission(context, {
                onGranted.invoke()
            }, {
                XXPermissionsNavHostUtil.startSettingManageStorage(context)
            })
        }
    }

    @OptIn(OPermission_WRITE_EXTERNAL_STORAGE::class, OPermission_MANAGE_EXTERNAL_STORAGE::class)
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