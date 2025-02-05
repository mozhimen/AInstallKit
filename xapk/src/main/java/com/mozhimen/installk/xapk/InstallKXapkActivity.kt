package com.mozhimen.installk.xapk

import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.mozhimen.uik.databinding.bases.viewbinding.activity.BaseActivityVB
import com.mozhimen.kotlin.elemk.android.app.cons.CPendingIntent
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.app.applyResult_ofOK
import com.mozhimen.kotlin.utilk.android.app.applyResult_ofCANCELED
import com.mozhimen.kotlin.utilk.android.content.UtilKIntent
import com.mozhimen.kotlin.utilk.android.content.UtilKPackageInstaller
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.android.widget.showToast
import com.mozhimen.installk.xapk.databinding.ActivityInstallBinding
import com.mozhimen.kotlin.utilk.android.content.UtilKPackageInstaller_Session
import kotlinx.coroutines.launch

/**
 * @ClassName InstallKXapkActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/18
 * @Version 1.0
 */
class InstallKXapkActivity : BaseActivityVB<ActivityInstallBinding>() {
    companion object {
        const val PACKAGE_INSTALLED_ACTION: String = "com.mozhimen.installk.xapk.SESSION_API_PACKAGE_INSTALLED"
        const val EXTRA_APK_PATH = "apk_path"
    }

    private val _strApkPathNames: List<String>? by lazy {
        intent.getStringArrayListExtra(EXTRA_APK_PATH).also {
            UtilKLogWrapper.d(TAG, "_strApkPathNames: apkFilePaths: $it")
        }
    }
    private val _packageInstallerSession: PackageInstaller.Session by lazy { UtilKPackageInstaller.getSession(this.applicationContext.packageManager.packageInstaller) }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    override fun initView(savedInstanceState: Bundle?) {
        if (!_strApkPathNames.isNullOrEmpty()) {
            //        if (isMeizu() || isVivo()) {
//            "魅族或VIVO系统用户如遇安装被中止或者安装失败的情况，请尝试联系手机平台客服，或者更换系统内置包安装器再重试".showToast()
//            finish()
//        }
            lifecycleScope.launch {
                try {
                    for (strApkPathName in _strApkPathNames!!) {
                        UtilKPackageInstaller_Session.addStrApkPathNameToSession(strApkPathName, _packageInstallerSession)
                    }
                    val intent = UtilKIntent.get<InstallKXapkActivity>(this@InstallKXapkActivity, block = {
                        setAction(PACKAGE_INSTALLED_ACTION)
                    })
                    UtilKPackageInstaller_Session.commit_use_ofActivity(_packageInstallerSession, intent, 0, CPendingIntent.FLAG_IMMUTABLE)
                } catch (e: Exception) {
                    e.printStackTrace()
                    UtilKPackageInstaller_Session.abandon_close(_packageInstallerSession)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val action = intent?.action
        val extras = intent?.extras
        Log.w(TAG, "onNewIntent: action $action extras $extras intent $intent")
        if (PACKAGE_INSTALLED_ACTION == action) {
            val status = extras?.getInt(PackageInstaller.EXTRA_STATUS)
            val message: String? = extras?.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
            UtilKLogWrapper.d(TAG, "onNewIntent: status $status message $message")
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    // This test app isn't privileged, so the user has to confirm the install.
                    val confirmIntent = extras[Intent.EXTRA_INTENT] as? Intent?
                    startActivity(confirmIntent)
                }

                PackageInstaller.STATUS_SUCCESS -> {
                    UtilKLogWrapper.d(TAG, "Install success")
                    "安装成功!".showToast()
                    applyResult_ofOK()
                }

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    UtilKLogWrapper.e(TAG, "Install failed! $status, $message")
                    "安装失败,请重试".showToast()
                    applyResult_ofCANCELED()
                }

                else -> {
                    UtilKLogWrapper.d(TAG, "Unrecognized status received from installer: $status")
                    "安装失败,解压文件可能已丢失或损坏，请重试".showToast()
                    applyResult_ofCANCELED()
                }
            }
        }
    }

//    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
//    override fun onDestroy() {
//        UtilKPackageInstallerSession.abandon_close(_packageInstallerSession)
//        super.onDestroy()
//    }
}