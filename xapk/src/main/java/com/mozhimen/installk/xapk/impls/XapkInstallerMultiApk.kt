package com.mozhimen.installk.xapk.impls

import android.content.Context
import com.mozhimen.kotlin.utilk.android.content.startContext
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.xapk.InstallKXapkActivity
import com.mozhimen.installk.xapk.bases.BaseXapkInstaller
import java.io.File

/**
 * <pre>
 *     author : wuliang
 *     time   : 2019/09/27
 * </pre>
 */
class XapkInstallerMultiApk(xapkPath: String, xapkUnzipOutputDir: File) : BaseXapkInstaller(xapkPath, xapkUnzipOutputDir) {

    override fun getUnzipPath(): String? =
        xapkUnzipOutputDir.absolutePath

    override fun install(xapkPath: String, context: Context) {
        val files = xapkUnzipOutputDir.listFiles()

        val apkFilePaths = files?.filter { file ->
            file.isFile && file.name.endsWith(".apk")
        }?.map { it.absolutePath } ?: emptyList()

        enterInstallActivity(xapkPath, ArrayList(apkFilePaths), context)
    }

    private fun enterInstallActivity(xapkPath: String, apkFilePaths: ArrayList<String>, context: Context) {
        UtilKLogWrapper.d(TAG, "enterInstallActivity----->")
        UtilKLogWrapper.d(TAG, "multi apk xapk installer,enter InstallActivity,xapkPath:$xapkPath")
        apkFilePaths.forEach {
            UtilKLogWrapper.d(TAG, "enterInstallActivity: apkFilePath: $it")
        }
        UtilKLogWrapper.d(TAG, "enterInstallActivity----->")
        context.startContext<InstallKXapkActivity> {
            putStringArrayListExtra(InstallKXapkActivity.EXTRA_APK_PATH, apkFilePaths)
        }
    }
}