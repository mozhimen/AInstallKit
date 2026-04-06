package com.mozhimen.installk.builder.smart

import android.Manifest
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.RequiresPermission
import com.mozhimen.installk.builder.auto.InstallKBuilderAuto
import com.mozhimen.installk.builder.basic.bases.BaseInstallKBuilder
import com.mozhimen.installk.builder.basic.cons.EInstallKMessage
import com.mozhimen.installk.builder.basic.cons.EInstallKType
import com.mozhimen.installk.builder.hand.InstallKBuilderHand
import com.mozhimen.installk.builder.root.InstallKBuilderRoot
import com.mozhimen.installk.builder.silence.InstallKBuilderSilence
import com.mozhimen.kotlin.elemk.android.cons.CPermission
import com.mozhimen.kotlin.elemk.android.os.cons.CVersCode
import com.mozhimen.kotlin.lintk.optins.device.ODeviceRoot
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_INSTALL_PACKAGES
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.android.util.e
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.wrapper.UtilKApp
import com.mozhimen.kotlin.utilk.wrapper.UtilKAppInstall
import com.mozhimen.kotlin.utilk.wrapper.UtilKPermission
import com.mozhimen.kotlin.utilk.wrapper.UtilKSys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @ClassName InstallKBuilderSmart
 * @Description TODO
 * @Author mozhimen
 * @Date 2026/4/3
 * @Version 1.0
 */
open class InstallKBuilderSmart : BaseInstallKBuilder() {
    private val tryChain = hashMapOf(
        EInstallKType.ROOT to ::tryInstall_root,
        EInstallKType.SILENCE to ::tryInstall_silence,
        EInstallKType.AUTO to ::tryInstall_auto,
        EInstallKType.HAND to ::tryInstall_hand
    )

    private var _silenceReceiverClazz: Class<*>? = null
    private var _accessibilityServiceClazz: Class<*>? = null

    fun setAccessibilityService(serviceClazz: Class<*>?): InstallKBuilderSmart {
        _accessibilityServiceClazz = serviceClazz
        return this
    }

    fun setInstallSilenceReceiver(receiverClazz: Class<*>?): InstallKBuilderSmart {
        _silenceReceiverClazz = receiverClazz
        return this
    }

    //MARK: -

    @OptIn(ODeviceRoot::class)
    protected fun tryInstall_root(strPathNameApk: String): Boolean {
        return try {
            InstallKBuilderRoot()
                .setInstallStateChangeListener(_iInstallStateChangeListener)
                .tryInstall(strPathNameApk)
        } catch (_: Exception) {
            false
        }
    }

    protected fun tryInstall_hand(strPathNameApk: String): Boolean {
        return try {
            InstallKBuilderHand()
                .setInstallStateChangeListener(_iInstallStateChangeListener)
                .tryInstall(strPathNameApk)
        } catch (_: Exception) {
            false
        }
    }

    protected fun tryInstall_silence(strPathNameApk: String): Boolean {
        try {
            if (_silenceReceiverClazz == null) return false
            return InstallKBuilderSilence()
                .setInstallSilenceReceiver(_silenceReceiverClazz!!)
                .setInstallStateChangeListener(_iInstallStateChangeListener)
                .tryInstall(strPathNameApk)
        } catch (_: Exception) {
            return false
        }
    }

    protected fun tryInstall_auto(strPathNameApk: String): Boolean {
        try {
            if (_accessibilityServiceClazz == null) return false
            return InstallKBuilderAuto()
                .setAccessibilityService(_accessibilityServiceClazz!!)
                .setInstallStateChangeListener(_iInstallStateChangeListener)
                .tryInstall(strPathNameApk)
        } catch (_: Exception) {
            return false
        }
    }

    //MARK: -

    override fun tryInstall(strPathNameApk: String): Boolean {
        require(strPathNameApk.isNotEmpty() && strPathNameApk.endsWith(".apk")) { "$TAG $strPathNameApk not a correct apk file path" }
        require(strPathNameApk.isFileExist()) { "$TAG $strPathNameApk is not exist" }

        for (t in tryChain) {
            val res = t.value.invoke(strPathNameApk)
            if (res) {
                return true
            }
        }

        return false
    }
}