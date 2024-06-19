package com.mozhimen.installk.xapk

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mozhimen.basick.elemk.android.app.cons.CActivity
import com.mozhimen.basick.elemk.androidx.appcompat.bases.BaseActivity
import com.mozhimen.basick.taskk.executor.TaskKExecutor
import com.mozhimen.basick.utilk.android.content.UtilKPackageInstaller
import com.mozhimen.basick.utilk.android.content.UtilKPackageInstallerSession
import java.io.IOException

/**
 * @ClassName InstallKXapkActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/18
 * @Version 1.0
 */
class InstallKXapkActivity : BaseActivity() {
    companion object {
        const val PACKAGE_INSTALLED_ACTION: String = "com.mozhimen.installk.xapk.SESSION_API_PACKAGE_INSTALLED"
        const val EXTRA_APK_PATH = "apk_path"
    }

    private val _strApkPathNames: List<String>? by lazy { intent.getStringArrayListExtra(EXTRA_APK_PATH) }
    private val _packageInstallerSession: PackageInstaller.Session by lazy { UtilKPackageInstaller.getSession(this.applicationContext.packageManager.packageInstaller) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install)
        installXapk()
    }

    private fun installXapk() {
        if (_strApkPathNames == null || _strApkPathNames!!.isEmpty()) {
            setResult(CActivity.RESULT_CANCELED)
            finish()
        }

//        if (isMeizu() || isVivo()) {
//            "魅族或VIVO系统用户如遇安装被中止或者安装失败的情况，请尝试联系手机平台客服，或者更换系统内置包安装器再重试".showToast()
//            finish()
//        }

        TaskKExecutor.execute(NAME) {
            try {
                for (strApkPathName in _strApkPathNames!!) {
                    UtilKPackageInstallerSession.addStrApkPathNameToSession(strApkPathName, _packageInstallerSession)
                }
                commitSession(_packageInstallerSession)
            } catch (e: IOException) {
                e.printStackTrace()
                abandonSession()
            }
        }
    }

    @TargetApi(21)
    private fun commitSession(session: PackageInstaller.Session?) {
        // Create an install status receiver.
        val intent = Intent(this, InstallActivity::class.java)
        intent.setAction(PACKAGE_INSTALLED_ACTION)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val statusReceiver = pendingIntent.intentSender

        // Commit the session (this will start the installation workflow).
        session!!.commit(statusReceiver)
    }

    @TargetApi(21)
    private fun abandonSession() {
        if (_packageInstallerSession != null) {
            _packageInstallerSession!!.abandon()
            _packageInstallerSession!!.close()
        }
    }

    @TargetApi(21)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val extras = intent.extras
        if (PACKAGE_INSTALLED_ACTION == intent.action) {
            var status = -100
            var message: String? = ""
            if (extras != null) {
                status = extras.getInt(PackageInstaller.EXTRA_STATUS)
                message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
            }
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    // This test app isn't privileged, so the user has to confirm the install.
                    val confirmIntent = extras!![Intent.EXTRA_INTENT] as Intent?
                    startActivity(confirmIntent)
                }

                PackageInstaller.STATUS_SUCCESS -> {
                    Toast.makeText(this, "安装成功!", Toast.LENGTH_SHORT).show()
                    finish()
                }

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    Toast.makeText(
                        this, "安装失败,请重试",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    Log.d(TAG, "Install failed! $status, $message")
                }

                else -> {
                    Toast.makeText(
                        this, "安装失败,解压文件可能已丢失或损坏，请重试",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    Log.d(TAG, "Unrecognized status received from installer: $status")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (installXapkExectuor != null && !installXapkExectuor!!.isShutdown) {
            installXapkExectuor!!.shutdown()
        }
        abandonSession()
    }
}