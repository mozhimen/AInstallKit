package com.mozhimen.installk.manager.helpers

import android.content.Context
import android.content.Intent
import com.mozhimen.basick.elemk.android.content.bases.BaseBroadcastReceiver
import com.mozhimen.basick.elemk.android.content.cons.CIntent
import com.mozhimen.basick.lintk.optins.permission.OPermission_QUERY_ALL_PACKAGES
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.android.content.getVersionCode
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.manager.commons.IPackagesChangeListener

/**
 * @ClassName InstallKReceiver
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
class InstallKReceiver(private val _iPackagesChangeListener: IPackagesChangeListener) : BaseBroadcastReceiver() {
    @OptIn(OPermission_QUERY_ALL_PACKAGES::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            intent?.let { intent ->
                intent.dataString?.let { dataString ->
                    UtilKLogWrapper.d(TAG, "onReceive: dataString $dataString")

                    val packageName = dataString.split(":")[1]
                    if (packageName.isEmpty()) return

                    UtilKLogWrapper.i(TAG, "onReceive: action ${intent.action} apkPackName $packageName")

                    when (intent.action) {
                        CIntent.ACTION_PACKAGE_ADDED, CIntent.ACTION_PACKAGE_REPLACED -> {//有应用发生变化，强制刷新应用
                            var packageInfo = UtilKPackage.getPackageInfo(packageName,0)
                            if (packageInfo==null){
                                packageInfo = UtilKPackage.getInstalledPackages(false).find { it.packageName == packageName }
                            }
                            if (packageInfo != null) {
                                UtilKLogWrapper.d(TAG, "onReceive: packageInfo != null")

                                _iPackagesChangeListener.onPackageAddOrReplace(packageInfo.versionName, packageInfo.getVersionCode())
                            } else {
                                UtilKLogWrapper.e(TAG, "onReceive: cant find packageInfo just now")

                                _iPackagesChangeListener.onPackageAddOrReplace(packageName, -1)
                            }
                        }

                        CIntent.ACTION_PACKAGE_REMOVED -> {//需要主动移除掉保存的应用
                            _iPackagesChangeListener.onPackageRemove(packageName)
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}