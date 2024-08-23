package com.mozhimen.installk.manager

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.mozhimen.basick.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_QUERY_ALL_PACKAGES
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.android.content.UtilKPackageInfo
import com.mozhimen.basick.utilk.android.content.getVersionCode
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.installk.manager.commons.IPackagesChangeListener
import com.mozhimen.installk.manager.helpers.InstallKReceiverProxy
import com.mozhimen.installk.manager.mos.PackageBundle
import com.mozhimen.installk.manager.utils.packageInfo2packageBundle
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName InstallKManager
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2023/12/30 17:14
 * @Version 1.0
 */
@OApiInit_InApplication
object InstallKManager : BaseUtilK()/*, LifecycleOwner*/ {

    private val _installedPackageBundles = ConcurrentHashMap<String, PackageBundle>()//用来保存包的信息
    private val _packagesChangeListeners = mutableListOf<IPackagesChangeListener>()
    private val _iPackagesChangeListener = object : IPackagesChangeListener {
        override fun onPackageAddOrReplace(packageName: String, versionCode: Int) {
            addOrUpdatePackage(packageName, versionCode)
        }

        override fun onPackageRemove(packageName: String) {
            removePackage(packageName)
        }
    }

    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    private val _installKReceiverProxy by lazy { InstallKReceiverProxy(_context, ProcessLifecycleOwner.get(), _iPackagesChangeListener) }

    /////////////////////////////////////////////////////////////////////////

    @OptIn(OPermission_QUERY_ALL_PACKAGES::class, OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    fun init(context: Context) {
        //注册
        _installKReceiverProxy.bindLifecycle(ProcessLifecycleOwner.get())
        //填充数据
        if (_installedPackageBundles.isEmpty()) {
            _installedPackageBundles.putAll(UtilKPackage.getInstalledPackages(false).map { it.packageName to it.packageInfo2packageBundle() })
            UtilKLogWrapper.d(TAG, "init: _installedPackageInfos packages ${_installedPackageBundles.values}")
        }
    }

    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    fun getInstallKReceiverProxy():InstallKReceiverProxy =
        _installKReceiverProxy

    /////////////////////////////////////////////////////////////////////////

    @JvmStatic
    fun registerPackagesChangeListener(listener: IPackagesChangeListener) {
        if (!_packagesChangeListeners.contains(listener)) {
            _packagesChangeListeners.add(listener)
        }
    }

    @JvmStatic
    fun unregisterPackagesChangeListener(listener: IPackagesChangeListener) {
        val indexOf = _packagesChangeListeners.indexOf(listener)
        if (indexOf >= 0)
            _packagesChangeListeners.removeAt(indexOf)
    }

    /////////////////////////////////////////////////////////////////////////

    fun getPackageBundle_ofPackageName(packageName: String): PackageBundle? {
        return _installedPackageBundles[packageName]
    }

    fun getPackageBundle_ofPackageName_versionCode(packageName: String, versionCode: Int): PackageBundle? {
        return getPackageBundle_ofPackageName(packageName).takeIf { it?.versionCode == versionCode }
    }

    fun getPackageBundle_ofPackageName_lessThanInstalledVersionCode(packageName: String, versionCode: Int): PackageBundle? {
        return getPackageBundle_ofPackageName(packageName).takeIf { it != null && (it.versionCode >= versionCode) }
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * 查询应用是否安装
     */
    @JvmStatic
    fun hasPackageName(packageName: String): Boolean =
        getPackageBundle_ofPackageName(packageName) != null

    @JvmStatic
    fun hasPackageName_versionCode(packageName: String, versionCode: Int): Boolean =
        getPackageBundle_ofPackageName_versionCode(packageName, versionCode) != null

    /**
     * 查询应用是否安装并且大于等于需要下载的版本
     */
    @JvmStatic
    fun hasPackageName_lessThanInstalledVersionCode(packageName: String, versionCode: Int): Boolean =
        getPackageBundle_ofPackageName_lessThanInstalledVersionCode(packageName, versionCode) != null

    /////////////////////////////////////////////////////////////////////////

    /**
     * 应用安装的时候调用
     */
    @JvmStatic
    fun addOrUpdatePackage(packageName: String, versionCode: Int) {
        UtilKLogWrapper.d(TAG, "onPackageAdded: packageName $packageName versionCode $versionCode")
        onPackagesAddOrReplace(packageName, versionCode)
        val packageBundle = getPackageBundle_ofPackageName_versionCode(packageName, versionCode)
        if (packageBundle != null && packageBundle.versionCode == versionCode) {
            UtilKLogWrapper.d(TAG, "onPackageAdded: packageName already has package")
            return
        } else if (packageBundle != null) {
            packageBundle.versionCode = versionCode
            _installedPackageBundles[packageName] = packageBundle
        } else {
            UtilKPackageInfo.get(_context, packageName, 0)?.let {
                UtilKLogWrapper.d(TAG, "onPackageAdded: packageName add packageName $packageName versionCode $versionCode")
                _installedPackageBundles[packageName] = it.packageInfo2packageBundle()
            } ?: run {
                UtilKLogWrapper.d(TAG, "onPackageAdded: packageName add packageName (not find) $packageName versionCode $versionCode")
                _installedPackageBundles[packageName] = PackageBundle(packageName, versionCode)
            }
        }
    }

    /**
     * 应用安装的时候调用
     */
    @JvmStatic
    fun addOrUpdatePackage(packageInfo: PackageInfo) {
        addOrUpdatePackage(packageInfo.packageName, packageInfo.getVersionCode())
    }

    /**
     * 应用卸载的时候调用
     */
    @JvmStatic
    fun removePackage(packageName: String) {
        UtilKLogWrapper.d(TAG, "onPackageRemoved: packageName $packageName")
        onPackagesRemove(packageName)
        if (!hasPackageName(packageName)) {
            UtilKLogWrapper.d(TAG, "onPackageRemoved: packageName already remove package")
            return
        }
        _installedPackageBundles.remove(packageName)
    }

    /**
     * 应用卸载的时候调用
     */
    @JvmStatic
    fun removePackage(packageInfo: PackageInfo) {
        removePackage(packageInfo.packageName)
    }

    /////////////////////////////////////////////////////////////////////////

//    private val _tempNeedAddList = LinkedList<PackageInfo>()//远端有,本地没有
//    private val _tempRepeatList = LinkedList<String>()//重复的
//    private val _tempNeedDeleteList = LinkedList<PackageInfo>()//本地有,远端没有
//
//    fun analyzePackageInfos(
//        remotePackageInfos: List<PackageInfo>,
//        localPackageInfos: List<PackageInfo>
//    ) {
//        clearList()
//        if (remotePackageInfos.isEmpty() && localPackageInfos.isNotEmpty()) {        //远端没有本地有, 说明远端清空, 本地同步清空
//            _tempNeedDeleteList.addAll(localPackageInfos)
//            this.deleteAll()
//            UtilKLogWrapper.d(TAG, "analyzePackageInfos: deleteAll")
//        } else if (localPackageInfos.isEmpty() && remotePackageInfos.isNotEmpty()) {//远端有, 本地没有, 本地同步加入
//            remotePackageInfos.forEach {
//                _tempNeedAddList.add(it)
//            }
//            this.extractMultiple(_tempNeedAddList)
//            UtilKLogWrapper.d(TAG, "analyzePackageInfos: addAll")
//        } else if (localPackageInfos.isNotEmpty() && remotePackageInfos.isNotEmpty()) {
//            //重复的部分->比较迷糊->后期再进一步筛选
//            remotePackageInfos.forEach { remote ->
//                localPackageInfos.forEach { local ->
//                    if (remote.packageName == local.packageName) {
//                        _tempRepeatList.add(remote.packageName)
//                    }
//                }
//            }
//            //先得到需要删除的人员
//            localPackageInfos.forEach {
//                if (!_tempRepeatList.containsBy { packageName -> packageName == it.packageName }) {
//                    _tempNeedDeleteList.add(it)
//                }
//            }
//            this.deleteList(_tempNeedDeleteList)
//            //再得到需要加入的部分的人员包括(本地库没有的+远端更改的人员,包括在repeat部分)
//            remotePackageInfos.forEach {
//                if (!_tempRepeatList.containsBy { packageName -> packageName == it.packageName }) {
//                    _tempNeedAddList.add(it)
//                }
//            }
//            for (packageName in _tempRepeatList) {
//                val localPackageInfo = localPackageInfos.find { it.packageName == packageName }
//                val remotePackageInfo = remotePackageInfos.find { it.packageName == packageName } ?: continue
//                if (localPackageInfo == null) {
//                    _tempNeedAddList.add(remotePackageInfo)
//                    continue
//                }
//                if (localPackageInfo.packageName != remotePackageInfo.packageName) {
//                    _tempNeedAddList.add(remotePackageInfo)
//                }
//            }
//            //最后, 提取并入库
//            this.extractMultiple(_tempNeedAddList)
//        }
//        UtilKLogWrapper.d(TAG, "analyzePackageInfos: tempNeedAddList ${_tempNeedAddList.joinT2listIgnoreRepeat { it.packageName }}")
////        UtilKLogWrapper.d(TAG, "analyzePackageInfos: tempRepeatList $_tempRepeatList")
//        UtilKLogWrapper.d(TAG, "analyzePackageInfos: tempNeedDeleteList ${_tempNeedDeleteList.joinT2listIgnoreRepeat { it.packageName }}")
//    }
//
//    private fun clearList() {
//        _tempNeedAddList.clear()
//        _tempRepeatList.clear()
//        _tempNeedDeleteList.clear()
//    }
//
//    private fun deleteAll() {
////        TaskKExecutor.execute(TAG, runnable = DeleteAllTask())
//        _installedPackageInfos.clear()
//    }
//
//    private fun extractMultiple(extractList: List<PackageInfo>) {
//        if (extractList.isEmpty()) return
////        TaskKExecutor.execute(TAG, runnable = ExtractMultipleTask(extractList, localPackageInfos))
//        extractList.forEach {
//            if (!hasPackageName(it)) {
//                addPackage(it)
//            }
//        }
//    }
//
//    private fun deleteList(deleteList: List<PackageInfo>) {
//        if (deleteList.isEmpty()) return
////        TaskKExecutor.execute(TAG, runnable = DeleteListTask(deleteList))
////        deleteList.forEach {
////            if (hasPackageName(it)) {
////                removePackage(it)
////            }
////        }
//    }

    private fun onPackagesAddOrReplace(packageName: String, versionCode: Int) {
        for (listener in _packagesChangeListeners) {
            UtilKLogWrapper.d(TAG, "onPackagesAddOrReplace: listener $listener")
            listener.onPackageAddOrReplace(packageName, versionCode)
        }
    }

    private fun onPackagesRemove(packageName: String) {
        for (listener in _packagesChangeListeners) {
            UtilKLogWrapper.d(TAG, "onPackagesRemove: listener $listener")
            listener.onPackageRemove(packageName)
        }
    }
}