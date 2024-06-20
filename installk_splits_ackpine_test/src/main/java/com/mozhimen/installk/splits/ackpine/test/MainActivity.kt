package com.mozhimen.installk.splits.ackpine.test

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.mozhimen.basick.elemk.androidx.appcompat.bases.viewbinding.BaseActivityVB
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.lintk.optins.permission.OPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.basick.lintk.optins.permission.OPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.lintk.optins.permission.OPermission_WRITE_EXTERNAL_STORAGE
import com.mozhimen.basick.utilk.kotlin.UtilKStrPath
import com.mozhimen.basick.utilk.kotlin.isFileNotExist
import com.mozhimen.basick.utilk.kotlin.strAssetName2file
import com.mozhimen.installk.splits.ackpine.test.databinding.ActivityMainBinding
import com.mozhimen.manifestk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsNavHostUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.solrudev.ackpine.exceptions.SplitPackageException
import ru.solrudev.ackpine.splits.ApkSplits.filterCompatible
import ru.solrudev.ackpine.splits.ApkSplits.throwOnInvalidSplitPackage
import ru.solrudev.ackpine.splits.ZippedApkSplits

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
    private fun install(zippedFileUri: Uri,context: Context) {
        val splits = ZippedApkSplits.getApksForUri(zippedFileUri, context) // reading APKs from a zipped file
            .filterCompatible(context) // filtering the most compatible splits
            .throwOnInvalidSplitPackage()
        val splitsList = try {
            splits.toList()
        } catch (exception: SplitPackageException) {
            println(exception)
            emptyList()
        }
    }
}