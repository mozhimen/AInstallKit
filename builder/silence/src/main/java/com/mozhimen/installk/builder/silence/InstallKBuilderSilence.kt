package com.mozhimen.installk.builder.silence

import android.Manifest
import android.os.*
import androidx.annotation.RequiresPermission
import com.mozhimen.installk.builder.basic.bases.BaseInstallKBuilder
import com.mozhimen.installk.builder.basic.cons.EInstallKMessage
import com.mozhimen.kotlin.elemk.android.cons.CPermission
import com.mozhimen.kotlin.elemk.android.os.cons.CVersCode
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_INSTALL_PACKAGES
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.wrapper.UtilKApp
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
class InstallKBuilderSilence : BaseInstallKBuilder() {

    private var _silenceReceiverClazz: Class<*>? = null

    /**
     * 设置安装
     * @param receiverClazz Class<*>
     * @return InstallK
     */
    fun setInstallSilenceReceiver(receiverClazz: Class<*>?): InstallKBuilderSilence {
        _silenceReceiverClazz = receiverClazz
        return this
    }

    @OptIn(OUsesPermission_READ_EXTERNAL_STORAGE::class, OUsesPermission_MANAGE_EXTERNAL_STORAGE::class, OUsesPermission_INSTALL_PACKAGES::class, OUsesPermission_REQUEST_INSTALL_PACKAGES::class)
    override fun tryInstall(strPathNameApk: String): Boolean {
        require(strPathNameApk.isNotEmpty() && strPathNameApk.endsWith(".apk")) { "$TAG $strPathNameApk not a correct apk file path" }
        require(strPathNameApk.isFileExist()) { "$TAG $strPathNameApk is not exist" }
        requireNotNull(_silenceReceiverClazz) { "$TAG silence receiver must not be null" }

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

        if (UtilKApplicationInfo.getTargetSdkVersion() >= CVersCode.V_26_8_O && UtilKBuildVersion.isAfterV_26_8_O() && !UtilKPermission.hasRequestInstallPackages()) {        // 允许安装应用
            UtilKLogWrapper.w(TAG, "tryInstall: MSG_REQUIRE_PERMISSION")
            sendMessage(EInstallKMessage.MSG_REQUIRE_PERMISSION(arrayOf(CPermission.REQUEST_INSTALL_PACKAGES)))
            return false
        }

        require(UtilKSys.isRoot() || !UtilKApp.isUserApp()) { "$TAG this device has not root or its system app" }

        UtilKAppInstall.install_ofSilence(strPathNameApk, _silenceReceiverClazz!!)
        UtilKLogWrapper.d(TAG, "tryInstall: SILENCE success")
        return true
    }
}