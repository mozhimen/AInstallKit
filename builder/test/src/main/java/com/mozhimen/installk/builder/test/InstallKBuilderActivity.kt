package com.mozhimen.installk.builder.test

import android.os.Bundle
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import androidx.lifecycle.lifecycleScope
import com.mozhimen.bindk.bases.activity.databinding.BaseActivityVDB
import com.mozhimen.kotlin.lintk.optins.ODeviceRoot
import com.mozhimen.kotlin.elemk.android.cons.CPermission
import com.mozhimen.manifestk.permission.ManifestKPermission
import com.mozhimen.manifestk.permission.annors.APermissionCheck
import com.mozhimen.kotlin.utilk.android.app.UtilKActivityStart
import com.mozhimen.kotlin.utilk.android.content.UtilKPackage
import com.mozhimen.kotlin.utilk.android.widget.showToast
import com.mozhimen.kotlin.utilk.kotlin.UtilKLazyJVM.lazy_ofNone
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrAsset
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrFile
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrPath
import com.mozhimen.kotlin.utilk.wrapper.UtilKAppInstall
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
@APermissionCheck(
    CPermission.READ_EXTERNAL_STORAGE,
    CPermission.WRITE_EXTERNAL_STORAGE,
    CPermission.INTERNET,
    CPermission.READ_INSTALL_SESSIONS,
)
class InstallKBuilderActivity : BaseActivityVDB<ActivityInstallkBinding>() {
    private val _strPathNameApk by lazy_ofNone { UtilKStrPath.Absolute.Internal.getFiles() + "/installk/componentktest.apk" }
    @OptIn(ODeviceRoot::class)
    private val _installK by lazy_ofNone { InstallKBuilder() }

    @OptIn(ODeviceRoot::class)
    override fun initView(savedInstanceState: Bundle?) {
        vdb.installkTxt.text = UtilKPackage.getVersionCode().toString()
        vdb.installkBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (!UtilKStrFile.isFileExist(_strPathNameApk)) {
                    UtilKStrAsset.strAssetName2file("componentktest.apk", _strPathNameApk, false)
                }
                delay(500)
                _installK.setInstallMode(EInstallKMode.ROOT).setInstallSmartService(InstallKBuilderService::class.java).setInstallSilenceReceiver(InstallKBuilderReceiver::class.java)
                    .setInstallStateChangeListener(object : IInstallKStateListener {
                        override fun onInstallStart() {
                            UtilKLogWrapper.d(TAG, "onInstallStart:")
                        }

                        override fun onInstallFinish() {
                            UtilKLogWrapper.d(TAG, "onInstallFinish:")
                        }

                        override fun onInstallFail(msg: String?) {
                            UtilKLogWrapper.e(TAG, "onInstallFail: ${msg ?: ""}")
                        }

                        override fun onNeedPermissions(type: EInstallKPermissionType) {
                            UtilKLogWrapper.w(TAG, "onNeedPermissions: $type")
                            when (type) {
                                EInstallKPermissionType.COMMON -> {
                                    ManifestKPermission.requestPermissions(this@InstallKBuilderActivity, onSuccess = { "权限申请成功".showToast() })
                                }

                                EInstallKPermissionType.INSTALL -> {
                                    UtilKAppInstall.startManageUnknownInstallSource(this@InstallKBuilderActivity)
                                }

                                EInstallKPermissionType.ACCESSIBILITY -> {
                                    UtilKActivityStart.startSettingAccessibilitySettings(this@InstallKBuilderActivity)
                                }

                                else -> {}
                            }
                        }

                    }).install(_strPathNameApk)
            }
        }
    }
}