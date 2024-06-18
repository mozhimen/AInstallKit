package com.mozhimen.installk.xapk.impls

import android.content.Context
import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.wrapper.UtilKAppInstall
import com.mozhimen.installk.xapk.bases.BaseXapkInstaller
import java.io.File

/**
 * <pre>
 *     author : wuliang
 *     time   : 2019/09/27
 * </pre>
 */
class XapkInstallerSingleApk(xapkPath: String, xapkUnzipOutputDir: File) : BaseXapkInstaller(xapkPath, xapkUnzipOutputDir) {

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    override fun install(xapkPath: String, context: Context) {
        val files: Array<File>? = xapkUnzipOutputDir.listFiles()

        files?.forEach { file ->
            if ((file.isFile && file.name.endsWith(".apk"))) {
                val filePath = file.absolutePath

                if (!filePath.isNullOrEmpty()) {
                    UtilKLogWrapper.d(TAG, "single apk xapk installer,openDownloadApk")
                    UtilKAppInstall.install_ofView(filePath)
                }
            }
        }
    }

    override fun getUnzipPath(): String? {
        return xapkUnzipOutputDir.listFiles()?.find { file ->
            file.isFile && file.name.endsWith(".apk")
        }?.absolutePath
    }
}