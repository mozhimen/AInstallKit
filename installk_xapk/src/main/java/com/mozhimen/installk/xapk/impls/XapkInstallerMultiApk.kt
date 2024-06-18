package com.mozhimen.installk.xapk.impls

import android.content.Context
import com.mozhimen.basick.utilk.android.content.startContext
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.xapk.InstallActivity
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
        UtilKLogWrapper.d(TAG, "multi apk xapk installer,enter InstallActivity,xapkPath:$xapkPath,apkFilePaths:$apkFilePaths")
        context.startContext<InstallActivity> {
            putStringArrayListExtra(InstallActivity.KEY_APK_PATHS, apkFilePaths)
        }
    }
}