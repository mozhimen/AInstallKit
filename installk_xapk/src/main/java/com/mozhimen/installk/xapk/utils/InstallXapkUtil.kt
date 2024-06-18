package com.mozhimen.installk.xapk.utils

import android.os.Environment
import androidx.annotation.RequiresPermission
import com.mozhimen.basick.lintk.optins.permission.OPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.basick.lintk.optins.permission.OPermission_WRITE_EXTERNAL_STORAGE
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.utilk.android.os.UtilKEnvironment
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.java.io.getFileNameNoExtension
import com.mozhimen.basick.utilk.java.io.isFileExist
import com.mozhimen.basick.utilk.kotlin.createFolder
import com.mozhimen.basick.utilk.kotlin.getStrFileParentPath
import com.mozhimen.basick.utilk.kotlin.strFilePath2file
import com.mozhimen.installk.xapk.bases.BaseXapkInstaller
import com.mozhimen.installk.xapk.impls.XapkInstallerMultiApk
import com.mozhimen.installk.xapk.impls.XapkInstallerSingleApk
import org.zeroturnaround.zip.NameMapper
import org.zeroturnaround.zip.ZipException
import org.zeroturnaround.zip.ZipUtil
import java.io.File

/**
 * @ClassName InstallXapkUtil
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/18
 * @Version 1.0
 */
object InstallXapkUtil : IUtilK {
    @JvmStatic
    @OPermission_WRITE_EXTERNAL_STORAGE
    @OPermission_MANAGE_EXTERNAL_STORAGE
    @RequiresPermission(allOf = [CPermission.MANAGE_EXTERNAL_STORAGE, CPermission.WRITE_EXTERNAL_STORAGE])
    fun createXapkInstaller(strFilePathNameXapk: String): BaseXapkInstaller? {
        if (strFilePathNameXapk.isEmpty()){
            UtilKLogWrapper.e(TAG, "createXapkInstaller: strFilePathNameXapk empty")
            return null
        }
        val fileXapk = strFilePathNameXapk.strFilePath2file()
        val strFolderPathUnzip = createFolder_ofUnzip(fileXapk)
        if (strFolderPathUnzip.isNullOrEmpty()){
            UtilKLogWrapper.e(TAG, "createXapkInstaller: strFolderPathUnzip empty")
            return null
        }

        val folderUnzip = File(strFolderPathUnzip)
        try {
            //只保留apk文件和Android/obb下的文件,以及json文件用于获取主包（当有多个apk时）
            ZipUtil.unpack(fileXapk, folderUnzip, NameMapper { name ->
                when {
                    name.endsWith(".apk") -> return@NameMapper name
                    else -> return@NameMapper null
                }
            })
        } catch (e: ZipException) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "createXapkInstaller: unpack ${e.message}")
            return null
        }

        val files = folderUnzip.listFiles()
        val apkSize = files?.count { file ->
            file.isFile && file.name.endsWith(".apk")
        } ?: 0

        if (!unzipObbFiles(fileXapk, File(createFolder_ofObb()))){
            UtilKLogWrapper.e(TAG, "createXapkInstaller: unzip fail" )
            return null
        }

        return if (apkSize > 1) {
            XapkInstallerMultiApk(strFilePathNameXapk, folderUnzip)
        } else {
            XapkInstallerSingleApk(strFilePathNameXapk, folderUnzip)
        }
    }

    private fun unzipObbFiles(xapkFile: File, strFolderPathUnzip: File): Boolean {
        val prefix = "Android/obb"
        try {
            //只保留apk文件和Android/obb下的文件,以及json文件用于获取主包（当有多个apk时）
            ZipUtil.unpack(xapkFile, strFolderPathUnzip, NameMapper { name ->
                when {
                    name.startsWith(prefix) -> return@NameMapper name.substring(prefix.length)
                    else -> return@NameMapper null
                }
            })
            UtilKLogWrapper.d(TAG, "unzip obb to Android/obb succeed")
            return true
        } catch (e: ZipException) {
            e.printStackTrace()
            return false
        }
    }

    @OPermission_WRITE_EXTERNAL_STORAGE
    @OPermission_MANAGE_EXTERNAL_STORAGE
    @RequiresPermission(allOf = [CPermission.MANAGE_EXTERNAL_STORAGE, CPermission.WRITE_EXTERNAL_STORAGE])
    private fun createFolder_ofObb(): String {
        val strFolderPathObb: String = if (UtilKEnvironment.isExternalStorageStateMounted()) {
            "${Environment.getExternalStorageDirectory().path}${File.separator}Android${File.separator}obb"
        } else {
            "${Environment.getDataDirectory().parentFile?.toString() ?: ""}${File.separator}Android${File.separator}obb"
        }
        strFolderPathObb.createFolder()
        return strFolderPathObb
    }

    private fun createFolder_ofUnzip(file: File): String? {
        if (!file.isFileExist())
            return null
        val strFolderPathUnzip: String = file.absolutePath.getStrFileParentPath() + file.getFileNameNoExtension()/* getFileNameNoExtension(file)*/
        return if (strFolderPathUnzip.createFolder()) strFolderPathUnzip else null
    }
}