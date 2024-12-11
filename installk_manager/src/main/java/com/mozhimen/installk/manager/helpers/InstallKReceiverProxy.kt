package com.mozhimen.installk.manager.helpers

import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseBroadcastReceiverProxy2
import com.mozhimen.kotlin.elemk.android.content.cons.CIntent
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_QUERY_ALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.content.UtilKPackage
import com.mozhimen.kotlin.utilk.wrapper.UtilKSysRom
import com.mozhimen.installk.manager.commons.IInstallKReceiverProxy
import com.mozhimen.installk.manager.commons.IPackagesChangeListener
import com.mozhimen.kotlin.utilk.android.content.gainVersionCode
import com.mozhimen.stackk.basic.commons.IStackKListener
import com.mozhimen.stackk.callback.StackKCb
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @ClassName InstallKReceiverProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
@OApiInit_InApplication
@OApiCall_BindLifecycle
@OApiInit_ByLazy
class InstallKReceiverProxy(
    context: Context,
    owner: LifecycleOwner,
    private val _iPackagesChangeListener: IPackagesChangeListener
) : BaseBroadcastReceiverProxy2(
    context, owner, InstallKReceiver(_iPackagesChangeListener),
    /*    if (UtilKSysRom.isMeizu())
            arrayOf(CIntent.ACTION_PACKAGE_REMOVED)
        else*/
    arrayOf(CIntent.ACTION_PACKAGE_ADDED, CIntent.ACTION_PACKAGE_REPLACED, CIntent.ACTION_PACKAGE_REMOVED)
), IStackKListener, IInstallKReceiverProxy {
    private var _packageNames: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    override fun addPackageName(packageName: String) {
        if (!_packageNames.contains(packageName)) {
            _packageNames.add(packageName)
        }
    }

    override fun registerReceiver() {
        if (UtilKSysRom.isMeizu()) {
            StackKCb.instance.addFrontBackListener(this)
        }
        val intentFilter = IntentFilter()
        if (_actions.isNotEmpty()) {
            for (action in _actions)
                intentFilter.addAction(action)
        }
        intentFilter.addDataScheme("package")
        _activity.registerReceiver(_receiver, intentFilter)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (UtilKSysRom.isMeizu()) {
            StackKCb.instance.removeFrontBackListener(this)
        }
        super.onDestroy(owner)
    }

    @OptIn(OPermission_QUERY_ALL_PACKAGES::class)
    override fun onChanged(isFront: Boolean, activity: Activity) {
        if (isFront && _packageNames.isNotEmpty()) {
            val iterator = _packageNames.iterator()
            while (iterator.hasNext()){
                val packageName = iterator.next()
                if (UtilKPackage.hasPackage(packageName, 0)) {
                    val packageInfo = UtilKPackage.getPackageInfo(_context.packageName, 0)
                    if (packageInfo != null) {
                        _iPackagesChangeListener.onPackageAddOrReplace(packageInfo.packageName, packageInfo.gainVersionCode())
                    } else {
                        _iPackagesChangeListener.onPackageAddOrReplace(packageName, -1)
                    }
                    iterator.remove()
                }
            }
        }
    }
}