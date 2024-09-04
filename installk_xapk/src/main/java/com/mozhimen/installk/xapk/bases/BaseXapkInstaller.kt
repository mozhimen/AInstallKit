package com.mozhimen.installk.xapk.bases

import android.content.Context
import com.mozhimen.kotlin.utilk.commons.IUtilK
import java.io.File

abstract class BaseXapkInstaller(val xapkPath: String, val xapkUnzipOutputDir: File) : IUtilK {

    fun installXapk(context: Context) {
        install(xapkPath, context)
    }

    internal abstract fun install(xapkPath: String, context: Context)

    abstract fun getUnzipPath(): String?
}