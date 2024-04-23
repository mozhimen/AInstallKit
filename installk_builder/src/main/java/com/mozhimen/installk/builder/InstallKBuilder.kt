package com.mozhimen.installk.builder

import android.os.*
import android.util.Log
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.elemk.android.os.cons.CVersCode
import com.mozhimen.basick.lintk.optins.ODeviceRoot
import com.mozhimen.basick.lintk.optins.permission.OPermission_INSTALL_PACKAGES
import com.mozhimen.basick.lintk.optins.permission.OPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.basick.utilk.android.content.UtilKApplicationInfo
import com.mozhimen.basick.utilk.android.os.UtilKBuildVersion
import com.mozhimen.basick.utilk.android.util.e
import com.mozhimen.basick.utilk.kotlin.isFileExist
import com.mozhimen.basick.utilk.wrapper.UtilKApp
import com.mozhimen.basick.utilk.wrapper.UtilKAppInstall
import com.mozhimen.basick.utilk.wrapper.UtilKPermission
import com.mozhimen.basick.utilk.wrapper.UtilKSys
import com.mozhimen.installk.builder.commons.IInstallKBuilder
import com.mozhimen.installk.builder.commons.IInstallKStateListener
import com.mozhimen.installk.builder.cons.CInstallKCons
import com.mozhimen.installk.builder.cons.EInstallKMode
import com.mozhimen.installk.builder.cons.EInstallKPermissionType
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
@ODeviceRoot
class InstallKBuilder : IInstallKBuilder, BaseUtilK() {

    private var _installMode = EInstallKMode.AUTO
    private var _installStateChangeListener: IInstallKStateListener? = null
    private var _smartServiceClazz: Class<*>? = null
    private var _silenceReceiverClazz: Class<*>? = null
    private val _handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                CInstallKCons.MSG_DOWNLOAD_START -> _installStateChangeListener?.onDownloadStart()
                CInstallKCons.MSG_INSTALL_START -> _installStateChangeListener?.onInstallStart()
                CInstallKCons.MSG_INSTALL_FINISH -> _installStateChangeListener?.onInstallFinish()
                CInstallKCons.MSG_INSTALL_FAIL -> _installStateChangeListener?.onInstallFail(msg.obj as String)
                CInstallKCons.MSG_NEED_PERMISSION -> _installStateChangeListener?.onNeedPermissions(msg.obj as EInstallKPermissionType)
            }
        }
    }

    /**
     * 设置安装
     * @param receiverClazz Class<*>
     * @return InstallK
     */
    override fun setInstallSilenceReceiver(receiverClazz: Class<*>): InstallKBuilder {
        _silenceReceiverClazz = receiverClazz
        return this
    }

    /**
     * 设置监听器
     * @param listener IInstallStateChangedListener
     */
    override fun setInstallStateChangeListener(listener: IInstallKStateListener): InstallKBuilder {
        _installStateChangeListener = listener
        return this
    }

    /**
     * 设置安装模式
     * @param mode EInstallMode
     * @return InstallK
     */
    override fun setInstallMode(mode: EInstallKMode): InstallKBuilder {
        _installMode = mode
        return this
    }

    /**
     * 设置智能安装服务
     * @param serviceClazz Class<*>
     * @return InstallK
     */
    override fun setInstallSmartService(serviceClazz: Class<*>): InstallKBuilder {
        _smartServiceClazz = serviceClazz
        return this
    }

    /**
     * 安装
     * @param strPathNameApk String
     */
    suspend fun install(strPathNameApk: String) {
        withContext(Dispatchers.Main) {
            try {
                _handler.sendEmptyMessage(CInstallKCons.MSG_INSTALL_START)
                installByMode(strPathNameApk)
            } catch (e: Exception) {
                e.printStackTrace()
                UtilKLogWrapper.e(TAG, "install: ${e.message}")
                e.message?.e(TAG)
                _handler.sendMessage(Message().apply {
                    what = CInstallKCons.MSG_INSTALL_FAIL
                    obj = e.message ?: ""
                })
            } finally {
                _handler.sendEmptyMessage(CInstallKCons.MSG_INSTALL_FINISH)
            }
        }
    }

    @OptIn(OPermission_READ_EXTERNAL_STORAGE::class, OPermission_REQUEST_INSTALL_PACKAGES::class, OPermission_INSTALL_PACKAGES::class)
    @Throws(Exception::class)
    private fun installByMode(strPathNameApk: String) {
        require(strPathNameApk.isNotEmpty() && strPathNameApk.endsWith(".apk")) { "$TAG $strPathNameApk not a correct apk file path" }
        require(strPathNameApk.isFileExist()) { "$TAG $strPathNameApk is not exist" }
        if (!UtilKPermission.isSelfGranted(CInstallKCons.PERMISSIONS)) {
            UtilKLogWrapper.w(TAG, "installByMode: onNeedPermissions PERMISSIONS")
            _handler.sendMessage(Message().apply {
                what = CInstallKCons.MSG_NEED_PERMISSION
                obj = EInstallKPermissionType.COMMON
            })
            return
        }
        val targetSdkVersion = UtilKApplicationInfo.getTargetSdkVersion_ofCxt(_context)
        if (targetSdkVersion >= CVersCode.V_26_8_O && UtilKBuildVersion.isAfterV_26_8_O() && !UtilKAppInstall.hasRequestInstallPackages()) {        // 允许安装应用
            UtilKLogWrapper.w(TAG, "installByMode: onNeedPermissions isAppInstallsPermissionEnable false")
            _handler.sendMessage(Message().apply {
                what = CInstallKCons.MSG_NEED_PERMISSION
                obj = EInstallKPermissionType.INSTALL
            })
            return
        }

        when (_installMode) {
            EInstallKMode.AUTO -> {
                //try install root
                if (UtilKSys.isRoot() && UtilKAppInstall.install_ofRuntime(strPathNameApk)) {
                    UtilKLogWrapper.d(TAG, "installByMode: AUTO as ROOT success")
                    return
                }
                //try install silence
                if (_silenceReceiverClazz != null && (UtilKSys.isRoot() || !UtilKApp.isUserApp(_context))) {
                    UtilKAppInstall.install_ofSilence(strPathNameApk, _silenceReceiverClazz!!)
                    UtilKLogWrapper.d(TAG, "installByMode: AUTO as SILENCE success")
                    return
                }
                //try install smart
                if (_smartServiceClazz != null && UtilKPermission.hasAccessibility(_smartServiceClazz!!)) {
                    UtilKAppInstall.install_ofView(strPathNameApk)
                    UtilKLogWrapper.d(TAG, "installByMode: AUTO as SMART success")
                    return
                }
                //try install hand
                UtilKAppInstall.install_ofView(strPathNameApk)
            }

            EInstallKMode.ROOT -> {
                require(UtilKSys.isRoot()) { "$TAG this device has not root" }
                UtilKAppInstall.install_ofRuntime(strPathNameApk)
                UtilKLogWrapper.d(TAG, "installByMode: ROOT success")
            }

            EInstallKMode.SILENCE -> {
                requireNotNull(_silenceReceiverClazz) { "$TAG silence receiver must not be null" }
                require(UtilKSys.isRoot() || !UtilKApp.isUserApp(_context)) { "$TAG this device has not root or its system app" }
                UtilKAppInstall.install_ofSilence(strPathNameApk, _silenceReceiverClazz!!)
                UtilKLogWrapper.d(TAG, "installByMode: SILENCE success")
            }

            EInstallKMode.SMART -> {
                requireNotNull(_smartServiceClazz) { "$TAG smart service must not be null" }
                if (!UtilKPermission.hasAccessibility(_smartServiceClazz!!)) {
                    UtilKLogWrapper.w(TAG, "installByMode: SMART isAccessibilityPermissionEnable false")
                    _handler.sendMessage(Message().apply {
                        what = CInstallKCons.MSG_NEED_PERMISSION
                        obj = EInstallKPermissionType.ACCESSIBILITY
                    })
                    return
                }
                UtilKAppInstall.install_ofView(strPathNameApk)
                UtilKLogWrapper.d(TAG, "installByMode: SMART success")
            }

            EInstallKMode.HAND -> {
                UtilKAppInstall.install_ofView(strPathNameApk)
                UtilKLogWrapper.d(TAG, "installByMode: HAND success")
            }
        }
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
//            installByMode(strPathNameApk)
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