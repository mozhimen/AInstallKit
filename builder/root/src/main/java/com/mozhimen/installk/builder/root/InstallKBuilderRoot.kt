package com.mozhimen.installk.builder.root

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mozhimen.installk.builder.basic.bases.BaseInstallKBuilder
import com.mozhimen.installk.builder.basic.cons.EInstallKMessage
import com.mozhimen.kotlin.elemk.android.cons.CPermission
import com.mozhimen.kotlin.lintk.optins.device.ODeviceRoot
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.wrapper.UtilKAppInstall
import com.mozhimen.kotlin.utilk.wrapper.UtilKPermission
import com.mozhimen.kotlin.utilk.wrapper.UtilKSys

/**
 * @ClassName InstallKBuilder
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/1/7 0:04
 * @Version 1.0
 */
@ODeviceRoot
class InstallKBuilderRoot : BaseInstallKBuilder() {

    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE])
    @OptIn(OUsesPermission_READ_EXTERNAL_STORAGE::class, OUsesPermission_MANAGE_EXTERNAL_STORAGE::class)
    override fun tryInstall(strPathNameApk: String): Boolean {
        require(strPathNameApk.isNotEmpty() && strPathNameApk.endsWith(".apk")) { "$TAG $strPathNameApk not a correct apk file path" }
        require(strPathNameApk.isFileExist()) { "$TAG $strPathNameApk is not exist" }

        if (!UtilKPermission.canReadExternalStorage()) {        // 允许安装应用
            UtilKLogWrapper.w(TAG, "tryInstall: MSG_REQUIRE_PERMISSION")
            sendMessage(
                EInstallKMessage.MSG_REQUIRE_PERMISSION(
                    if (UtilKBuildVersion.isAfterV_30_11_R()) {
                        arrayOf(CPermission.READ_EXTERNAL_STORAGE, CPermission.MANAGE_EXTERNAL_STORAGE)
                    } else {
                        arrayOf(CPermission.READ_EXTERNAL_STORAGE)
                    }
                )
            )
            return false
        }

        require(UtilKSys.isRoot()) { "$TAG this device has not root" }

        return UtilKAppInstall.install_ofRoot(strPathNameApk).also {
            UtilKLogWrapper.d(TAG, "tryInstall: ROOT success")
        }
    }
}