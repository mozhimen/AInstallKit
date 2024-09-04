package com.mozhimen.installk.manager.utils

import android.content.pm.PackageInfo
import com.mozhimen.kotlin.utilk.android.content.UtilKPackageInfo
import com.mozhimen.installk.manager.mos.PackageBundle

/**
 * @ClassName InstallKManagerUtil
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/19
 * @Version 1.0
 */
fun PackageInfo.packageInfo2packageBundle(): PackageBundle =
    InstallKManagerUtil.packageInfo2packageBundle(this)

////////////////////////////////////////////////////////////////

object InstallKManagerUtil {
    @JvmStatic
    fun packageInfo2packageBundle(packageInfo: PackageInfo): PackageBundle =
        PackageBundle(packageInfo.packageName, UtilKPackageInfo.getVersionCode(packageInfo))
}