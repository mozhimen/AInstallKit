package com.mozhimen.installk.builder

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mozhimen.installk.builder.auto.InstallKBuilderAuto
import com.mozhimen.installk.builder.basic.bases.BaseInstallKBuilder
import com.mozhimen.installk.builder.basic.cons.EInstallKType
import com.mozhimen.installk.builder.hand.InstallKBuilderHand
import com.mozhimen.installk.builder.root.InstallKBuilderRoot
import com.mozhimen.installk.builder.silence.InstallKBuilderSilence
import com.mozhimen.installk.builder.smart.InstallKBuilderSmart
import com.mozhimen.kotlin.lintk.optins.device.ODeviceRoot
import com.mozhimen.kotlin.utilk.kotlin.isFileExist

/**
 * @ClassName InstallKBuilder
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/1/7 0:04
 * @Version 1.0
 */
class InstallKBuilder : BaseInstallKBuilder() {

    private var _installType = EInstallKType.SMART
    private var _accessibilityServiceClazz: Class<*>? = null
    private var _silenceReceiverClazz: Class<*>? = null

    fun setAccessibilityService(serviceClazz: Class<*>): InstallKBuilder {
        _accessibilityServiceClazz = serviceClazz
        return this
    }

    fun setInstallSilenceReceiver(receiverClazz: Class<*>): InstallKBuilder {
        _silenceReceiverClazz = receiverClazz
        return this
    }

    fun setInstallType(type: EInstallKType): InstallKBuilder {
        _installType = type
        return this
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE])
    @OptIn(ODeviceRoot::class)
    override fun tryInstall(strPathNameApk: String): Boolean {
        require(strPathNameApk.isNotEmpty() && strPathNameApk.endsWith(".apk")) { "$TAG $strPathNameApk not a correct apk file path" }
        require(strPathNameApk.isFileExist()) { "$TAG $strPathNameApk is not exist" }

        when (_installType) {
            EInstallKType.SMART -> {
                return InstallKBuilderSmart()
                    .setInstallSilenceReceiver(_silenceReceiverClazz)
                    .setAccessibilityService(_accessibilityServiceClazz)
                    .setInstallStateChangeListener(_iInstallStateChangeListener)
                    .tryInstall(strPathNameApk)
            }

            EInstallKType.ROOT -> {
                return InstallKBuilderRoot()
                    .setInstallStateChangeListener(_iInstallStateChangeListener)
                    .tryInstall(strPathNameApk)
            }

            EInstallKType.SILENCE -> {
                return InstallKBuilderSilence()
                    .setInstallSilenceReceiver(_silenceReceiverClazz)
                    .setInstallStateChangeListener(_iInstallStateChangeListener)
                    .tryInstall(strPathNameApk)
            }

            EInstallKType.AUTO -> {
                return InstallKBuilderAuto()
                    .setAccessibilityService(_accessibilityServiceClazz)
                    .setInstallStateChangeListener(_iInstallStateChangeListener)
                    .tryInstall(strPathNameApk)
            }

            EInstallKType.HAND ->{
                return InstallKBuilderHand()
                    .setInstallStateChangeListener(_iInstallStateChangeListener)
                    .tryInstall(strPathNameApk)
            }
        }
    }
}