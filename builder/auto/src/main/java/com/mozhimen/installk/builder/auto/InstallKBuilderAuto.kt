package com.mozhimen.installk.builder.auto

import android.os.*
import com.mozhimen.installk.builder.basic.bases.BaseInstallKBuilder
import com.mozhimen.installk.builder.basic.commons.IInstallKStateListener
import com.mozhimen.installk.builder.basic.cons.EInstallKMessage
import com.mozhimen.kotlin.elemk.android.cons.CPermission
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.elemk.android.os.cons.CVersCode
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.wrapper.UtilKAppInstall
import com.mozhimen.kotlin.utilk.wrapper.UtilKPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

/**
 * @ClassName InstallKBuilder
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/1/7 0:04
 * @Version 1.0
 */
class InstallKBuilderAuto : BaseInstallKBuilder() {

    private var _accessibilityServiceClazz: Class<*>? = null

    /**
     * 设置智能安装服务
     * @param serviceClazz Class<*>
     * @return InstallK
     */
    fun setAccessibilityService(serviceClazz: Class<*>?): InstallKBuilderAuto {
        _accessibilityServiceClazz = serviceClazz
        return this
    }

    @OptIn(OUsesPermission_REQUEST_INSTALL_PACKAGES::class)
    override fun tryInstall(strPathNameApk: String): Boolean {
        require(strPathNameApk.isNotEmpty() && strPathNameApk.endsWith(".apk")) { "$TAG $strPathNameApk not a correct apk file path" }
        require(strPathNameApk.isFileExist()) { "$TAG $strPathNameApk is not exist" }
        requireNotNull(_accessibilityServiceClazz) { "$TAG accessibilityService must not be null" }

        if (UtilKApplicationInfo.getTargetSdkVersion() >= CVersCode.V_26_8_O && UtilKBuildVersion.isAfterV_26_8_O() && !UtilKPermission.hasRequestInstallPackages()) {        // 允许安装应用
            UtilKLogWrapper.w(TAG, "tryInstall: MSG_REQUIRE_PERMISSION")
            sendMessage(EInstallKMessage.MSG_REQUIRE_PERMISSION(arrayOf(CPermission.REQUEST_INSTALL_PACKAGES)))
            return false
        }

        if (!UtilKPermission.hasAccessibility(_accessibilityServiceClazz!!)) {
            UtilKLogWrapper.w(TAG, "tryInstall: !hasAccessibility")
            sendMessage(EInstallKMessage.MSG_INSTALL_FAIL("没有配置无障碍"))
            return false
        }

        UtilKAppInstall.install_ofView(strPathNameApk)
        UtilKLogWrapper.d(TAG, "tryInstall: AUTO as SMART success")
        return true
    }

//    /**
//     * 下载并安装
//     * @param apkUrl String
//     */
//    suspend fun downloadFromUrlAndInstall(apkUrl: String) {
//        try {
//            _handler.sendEmptyMessage(CCons.MSG_DOWNLOAD_START)
//            var strPathNameApk: String
//            withContext(Dispatchers.IO) {
//                strPathNameApk = UtilKFileNet.downLoadFile(apkUrl, _tempStrPathNameApk)
//            }
//            tryInstall(strPathNameApk)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            UtilKLogWrapper.e(TAG, "downloadFromUrlAndInstall: ${e.message}")
//            _handler.sendMessage(Message().apply {
//                what = CCons.MSG_INSTALL_FAIL
//                obj = e.message ?: ""
//            })
//        } finally {
//            _handler.sendEmptyMessage(CCons.MSG_INSTALL_FINISH)
//        }
//    }
}