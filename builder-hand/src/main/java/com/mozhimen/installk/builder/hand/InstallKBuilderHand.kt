package com.mozhimen.installk.builder.hand

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mozhimen.installk.builder.basic.bases.BaseInstallKBuilder
import com.mozhimen.installk.builder.basic.cons.EInstallKMessage
import com.mozhimen.kotlin.elemk.android.cons.CPermission
import com.mozhimen.kotlin.elemk.android.os.cons.CVersCode
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.wrapper.UtilKAppInstall
import com.mozhimen.kotlin.utilk.wrapper.UtilKPermission

/**
 * @ClassName InstllKBuilderHand
 * @Description TODO
 * @Author mozhimen
 * @Date 2026/4/3
 * @Version 1.0
 */
class InstallKBuilderHand : BaseInstallKBuilder() {

    @OptIn(OUsesPermission_REQUEST_INSTALL_PACKAGES::class)
    @RequiresPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES)
    override fun tryInstall(strPathNameApk: String): Boolean {
        require(strPathNameApk.isNotEmpty() && strPathNameApk.endsWith(".apk")) { "$TAG $strPathNameApk not a correct apk file path" }
        require(strPathNameApk.isFileExist()) { "$TAG $strPathNameApk is not exist" }

        if (UtilKApplicationInfo.getTargetSdkVersion() >= CVersCode.V_26_8_O && UtilKBuildVersion.isAfterV_26_8_O() && !UtilKPermission.hasRequestInstallPackages()) {        // 允许安装应用
            UtilKLogWrapper.w(TAG, "tryInstall: MSG_REQUIRE_PERMISSION")
            sendMessage(EInstallKMessage.MSG_REQUIRE_PERMISSION(arrayOf(CPermission.REQUEST_INSTALL_PACKAGES)))
            return false
        }

        //try install hand
        UtilKAppInstall.install_ofView(strPathNameApk)
        UtilKLogWrapper.d(TAG, "tryInstall: HAND success")
        return true
    }
}