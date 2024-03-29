package com.mozhimen.installk.manager

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_QUERY_ALL_PACKAGES
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.android.content.UtilKPackageInfo
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.basick.utilk.kotlin.collections.containsBy
import com.mozhimen.installk.manager.commons.IPackagesChangeListener

/**
 * @ClassName InstallKManager
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2023/12/30 17:14
 * @Version 1.0
 */
@OApiInit_InApplication
object InstallKManager : BaseUtilK()/*, LifecycleOwner*/ {

    private val _installedPackageInfos = mutableListOf<PackageInfo>()//用来保存包的信息

//    @OptIn(OptInApiCall_BindLifecycle::class, OptInApiInit_ByLazy::class)
//    private val _taskKPollInfinite: TaskKPollInfinite by lazy { TaskKPollInfinite() }

    private val _packagesChangeListeners = mutableListOf<IPackagesChangeListener>()

//    private var _lifecycleRegistry: LifecycleRegistry? = null
//    private val lifecycleRegistry: LifecycleRegistry
//        get() = _lifecycleRegistry ?: LifecycleRegistry(this).also {
//            _lifecycleRegistry = it
//        }

    /////////////////////////////////////////////////////////////////////////

    @OptIn(OPermission_QUERY_ALL_PACKAGES::class)
    fun init(context: Context) {
//        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        if (_installedPackageInfos.isEmpty()) {
            _installedPackageInfos.addAll(UtilKPackage.getInstalledPackages(context, false).also {
                Log.d(TAG, "init: _installedPackageInfos packages ${it.map { packageInfo -> packageInfo.packageName }}")
            })
        }
//        _taskKPollInfinite.apply {
//            bindLifecycle(this@InstallKManager)
//            start(10000) {
//                analyzePackageInfos(UtilKPackageManager.getInstalledPackages(context, false), _installedPackageInfos)
//            }
//        }
    }

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

    fun getPackageInfoByPackageName(packageName: String): PackageInfo? {
        return _installedPackageInfos.find { it.packageName == packageName }
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * 查询应用是否安装
     */
    @JvmStatic
    fun hasPackageName(packageName: String): Boolean =
        _installedPackageInfos.containsBy { exist -> packageName == exist.packageName }

    /**
     * 查询应用是否安装
     */
    @JvmStatic
    fun hasPackageName(packageInfo: PackageInfo): Boolean =
        _installedPackageInfos.containsBy { exist -> packageInfo.packageName == exist.packageName }

    /**
     * 查询应用是否安装并且大于等于需要下载的版本
     */
    @JvmStatic
    fun hasPackageNameAndSatisfyVersion(packageName: String, versionCode: Int): Boolean =
        _installedPackageInfos.containsBy { existPackageInfo -> packageName == existPackageInfo.packageName && versionCode <= UtilKPackageInfo.getVersionCode(existPackageInfo) }

    /////////////////////////////////////////////////////////////////////////

    /**
     * 应用安装的时候调用
     */
    @JvmStatic
    fun addPackage(packageName: String) {
        Log.d(TAG, "onPackageAdded: packageName $packageName")
        if (hasPackageName(packageName)) {
            Log.d(TAG, "onPackageAdded: packageName already has package")
            return
        }
        UtilKPackageInfo.get(_context, packageName, 0)?.let {
            Log.d(TAG, "onPackageAdded: packageName add packageName $packageName")
            _installedPackageInfos.add(it)
        }
    }

    /**
     * 应用安装的时候调用
     */
    @JvmStatic
    fun addPackage(packageInfo: PackageInfo) {
        Log.d(TAG, "onPackageAdded: packageInfo ${packageInfo.packageName}")
        if (hasPackageName(packageInfo)) {
            Log.d(TAG, "onPackageAdded: packageInfo already has package")
            return
        }
        onPackagesAdd(packageInfo)
        Log.d(TAG, "onPackageAdded: packageInfo add packageName ${packageInfo.packageName}")
        _installedPackageInfos.add(packageInfo)
    }

    /**
     * 应用卸载的时候调用
     */
    @JvmStatic
    fun removePackage(packageName: String) {
        Log.d(TAG, "onPackageRemoved: packageName $packageName")
        if (!hasPackageName(packageName)) {
            Log.d(TAG, "onPackageRemoved: packageName already remove package")
            return
        }
        val iterator = _installedPackageInfos.iterator()
        while (iterator.hasNext()) {
            val packageInfo = iterator.next()
            if (packageInfo.packageName == packageName) {
                Log.d(TAG, "onPackageRemoved: packageName remove packageName $packageName")
                iterator.remove()
                break
            }
        }
    }

    /**
     * 应用卸载的时候调用
     */
    @JvmStatic
    fun removePackage(packageInfo: PackageInfo) {
        Log.d(TAG, "onPackageRemoved: packageInfo ${packageInfo.packageName}")
        if (!hasPackageName(packageInfo)) {
            Log.d(TAG, "onPackageRemoved: packageInfo already remove package")
            return
        }
        onPackagesRemove(packageInfo)
        val iterator = _installedPackageInfos.iterator()
        while (iterator.hasNext()) {
            val localPackageInfo = iterator.next()
            if (localPackageInfo.packageName == packageInfo.packageName) {
                Log.d(TAG, "onPackageRemoved: packageInfo remove packageName ${packageInfo.packageName}")
                iterator.remove()
                break
            }
        }
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
//            Log.d(TAG, "analyzePackageInfos: deleteAll")
//        } else if (localPackageInfos.isEmpty() && remotePackageInfos.isNotEmpty()) {//远端有, 本地没有, 本地同步加入
//            remotePackageInfos.forEach {
//                _tempNeedAddList.add(it)
//            }
//            this.extractMultiple(_tempNeedAddList)
//            Log.d(TAG, "analyzePackageInfos: addAll")
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
//        Log.d(TAG, "analyzePackageInfos: tempNeedAddList ${_tempNeedAddList.joinT2listIgnoreRepeat { it.packageName }}")
////        Log.d(TAG, "analyzePackageInfos: tempRepeatList $_tempRepeatList")
//        Log.d(TAG, "analyzePackageInfos: tempNeedDeleteList ${_tempNeedDeleteList.joinT2listIgnoreRepeat { it.packageName }}")
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

    private fun onPackagesAdd(packageInfo: PackageInfo) {
        for (listener in _packagesChangeListeners) {
            listener.onPackageAdd(packageInfo)
        }
    }

    private fun onPackagesRemove(packageInfo: PackageInfo) {
        for (listener in _packagesChangeListeners) {
            listener.onPackageRemove(packageInfo)
        }
    }


//    override val lifecycle: Lifecycle
//        get() = lifecycleRegistry
}