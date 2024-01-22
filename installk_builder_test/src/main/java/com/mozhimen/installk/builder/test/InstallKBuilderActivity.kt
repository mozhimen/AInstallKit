package com.mozhimen.installk.builder.test

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.lintk.optin.OptInDeviceRoot
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.basick.manifestk.cons.CManifest
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.basick.utilk.android.app.UtilKLaunchActivity
import com.mozhimen.basick.utilk.android.content.UtilKAppInstall
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.android.widget.showToast
import com.mozhimen.basick.utilk.kotlin.UtilKStrAsset
import com.mozhimen.basick.utilk.kotlin.UtilKStrFile
import com.mozhimen.basick.utilk.kotlin.UtilKStrPath
import com.mozhimen.installk.builder.InstallKBuilder
import com.mozhimen.installk.builder.commons.IInstallKStateListener
import com.mozhimen.installk.builder.cons.EInstallKMode
import com.mozhimen.installk.builder.cons.EInstallKPermissionType
import com.mozhimen.installk.builder.test.databinding.ActivityInstallkBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * @ClassName InstallKActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/1/12 14:02
 * @Version 1.0
 */
@AManifestKRequire(
    CPermission.READ_EXTERNAL_STORAGE,
    CPermission.WRITE_EXTERNAL_STORAGE,
    CPermission.INTERNET,
    CPermission.REQUEST_INSTALL_PACKAGES,
    CPermission.INSTALL_PACKAGES,
    CPermission.READ_INSTALL_SESSIONS,
    CPermission.REPLACE_EXISTING_PACKAGE,
    CPermission.BIND_ACCESSIBILITY_SERVICE,
    CManifest.SERVICE_ACCESSIBILITY
)
@APermissionCheck(
    CPermission.READ_EXTERNAL_STORAGE,
    CPermission.WRITE_EXTERNAL_STORAGE,
    CPermission.INTERNET,
    CPermission.READ_INSTALL_SESSIONS,
)
class InstallKBuilderActivity : BaseActivityVB<ActivityInstallkBinding>() {
    private val _strPathNameApk by lazy { UtilKStrPath.Absolute.Internal.getFiles() + "/installk/componentktest.apk" }
    @OptIn(OptInDeviceRoot::class)
    private val _installK by lazy { InstallKBuilder() }

    @OptIn(OptInDeviceRoot::class)
    override fun initView(savedInstanceState: Bundle?) {
        vb.installkTxt.text = UtilKPackage.getVersionCode().toString()
        vb.installkBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (!UtilKStrFile.isFileExist(_strPathNameApk)) {
                    UtilKStrAsset.strAssetName2file("componentktest.apk", _strPathNameApk, false)
                }
                delay(500)
                _installK.setInstallMode(EInstallKMode.ROOT).setInstallSmartService(InstallKBuilderService::class.java).setInstallSilenceReceiver(InstallKBuilderReceiver::class.java)
                    .setInstallStateChangeListener(object : IInstallKStateListener {
                        override fun onInstallStart() {
                            Log.d(TAG, "onInstallStart:")
                        }

                        override fun onInstallFinish() {
                            Log.d(TAG, "onInstallFinish:")
                        }

                        override fun onInstallFail(msg: String?) {
                            Log.e(TAG, "onInstallFail: ${msg ?: ""}")
                        }

                        override fun onNeedPermissions(type: EInstallKPermissionType) {
                            Log.w(TAG, "onNeedPermissions: $type")
                            when (type) {
                                EInstallKPermissionType.COMMON -> {
                                    ManifestKPermission.requestPermissions(this@InstallKBuilderActivity, onSuccess = { "权限申请成功".showToast() })
                                }

                                EInstallKPermissionType.INSTALL -> {
                                    UtilKAppInstall.openSettingAppInstall(this@InstallKBuilderActivity)
                                }

                                EInstallKPermissionType.ACCESSIBILITY -> {
                                    UtilKLaunchActivity.startSettingAccessibility(this@InstallKBuilderActivity)
                                }

                                else -> {}
                            }
                        }

                    }).install(_strPathNameApk)
            }
        }
    }
}