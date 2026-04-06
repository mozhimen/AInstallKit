package com.mozhimen.installk.splits.ackpine.test

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_MANAGE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_POST_NOTIFICATIONS
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_READ_EXTERNAL_STORAGE
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.lintk.optins.manifest.uses_permission.OUsesPermission_WRITE_EXTERNAL_STORAGE
import com.mozhimen.kotlin.utilk.android.net.getDisplayName
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrPath
import com.mozhimen.kotlin.utilk.kotlin.isFileNotExist
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2uri
import com.mozhimen.installk.splits.ackpine.InstallKSplitsAckpine
import com.mozhimen.installk.splits.ackpine.test.databinding.ActivityMainBinding
import com.mozhimen.kotlin.utilk.kotlin.strAssetName2file_use
import com.mozhimen.permissionk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsNavHostUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsRequestUtil
import com.mozhimen.uik.databinding.bases.viewbinding.activity.BaseActivityVBVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.session.ProgressSession
import ru.solrudev.ackpine.session.SessionResult
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.progress
import ru.solrudev.ackpine.splits.Apk
import ru.solrudev.ackpine.splits.ApkSplits.filterCompatible
import ru.solrudev.ackpine.splits.ApkSplits.throwOnInvalidSplitPackage
import ru.solrudev.ackpine.splits.ZippedApkSplits

class MainActivity : BaseActivityVBVM<ActivityMainBinding, MainViewModel>() {

    private val _strXApkPathName by lazy { UtilKStrPath.Absolute.Internal.getFiles() + "/" + "test.xapk" }

    override fun getViewModelProviderFactory(): ViewModelProvider.Factory? {
        return MainViewModel.Factory
    }

    @SuppressLint("MissingPermission")
    @OptIn(OUsesPermission_WRITE_EXTERNAL_STORAGE::class, OUsesPermission_MANAGE_EXTERNAL_STORAGE::class, OUsesPermission_POST_NOTIFICATIONS::class)
    override fun initView(savedInstanceState: Bundle?) {
        vb.installTv.setOnClickListener {
            applyPermissionInstall(this) {
                applyPermissionStorage(this) {
                    applyPermissionNotification(this) {
                        if (_strXApkPathName.isFileNotExist()) {
                            "test.xapk".strAssetName2file_use(_strXApkPathName)
                        }
                        InstallKSplitsAckpine.install(_strXApkPathName.strFilePath2uri() ?: kotlin.run {
                            UtilKLogWrapper.d(TAG, "initView: _strXApkPathName uri is null")
                            return@applyPermissionNotification
                        }, this.lifecycleScope) {
                            UtilKLogWrapper.d(TAG, "initView: $it")
                        }
                    }
                }
            }
        }
    }

    override fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { uiState ->
                    if (!uiState.error.isEmpty) {
                        Snackbar.make(vb.root, uiState.error.resolve(this@MainActivity), Snackbar.LENGTH_LONG)
                            .show()
                        vm.clearError()
                    }
                }
            }
        }
    }

    @OUsesPermission_WRITE_EXTERNAL_STORAGE
    @OUsesPermission_MANAGE_EXTERNAL_STORAGE
    private fun install(uri: Uri) {
        val name = uri.getDisplayName()
        val apks = getApksFromUri(uri, name)
        UtilKLogWrapper.d(TAG, "install: name $name apks $apks")
        installPackage(apks, name)
    }

    private fun getApksFromUri(uri: Uri, name: String): Sequence<Apk> {
        val extension = name.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "apk" -> sequence { Apk.fromUri(uri, applicationContext)?.let { yield(it) } }.constrainOnce()
            "zip", "apks", "xapk", "apkm" -> ZippedApkSplits.getApksForUri(uri, applicationContext)
                .filterCompatible(applicationContext)
                .throwOnInvalidSplitPackage()

            else -> emptySequence()
        }
    }

    fun installPackage(apks: Sequence<Apk>, fileName: String) = lifecycleScope.launch {
        val uris = runInterruptible(Dispatchers.IO) { apks.toUrisList() }
        if (uris.isEmpty()) {
            UtilKLogWrapper.d(TAG, "installPackage: uris is Empty")
            return@launch
        }
        val session = PackageInstaller.getInstance(application).createSession(uris) {
            name = fileName
            requireUserAction = false
        }
        awaitSession(session)
    }

    private fun awaitSession(session: ProgressSession<InstallFailure>) = lifecycleScope.launch {
        session.progress
            .onEach { progress -> UtilKLogWrapper.d(TAG, "awaitSession: session ${session.id} progress ${session.progress}") /*sessionDataRepository.updateSessionProgress(session.id, progress)*/ }
            .launchIn(this)
        try {
            when (val result = session.await()) {
                is SessionResult.Success -> UtilKLogWrapper.d(TAG, "awaitSession: ")/*sessionDataRepository.removeSessionData(session.id)*/
                is SessionResult.Error -> UtilKLogWrapper.d(TAG, "awaitSession: ${result.cause}")/*handleSessionError(result.cause.message, session.id)*/
            }
        } catch (e: Exception) {
//            handleSessionError(e.message, session.id)
            UtilKLogWrapper.e(TAG, "awaitSession ", e)
        }
    }

    private fun Sequence<Apk>.toUrisList(): List<Uri> =
        try {
            map { it.uri }.toList()
        } catch (exception: Exception) {
            emptyList()
        }

    @OptIn(OUsesPermission_REQUEST_INSTALL_PACKAGES::class)
    private fun applyPermissionInstall(context: Context, onGranted: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_REQUEST_INSTALL_PACKAGES(context)) {
            onGranted.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_REQUEST_INSTALL_PACKAGES(context, {
                onGranted.invoke()
            }, {
                XXPermissionsNavHostUtil.startPermission_REQUEST_INSTALL_PACKAGES(context)
            })
        }
    }

    @OptIn(OUsesPermission_READ_EXTERNAL_STORAGE::class, OUsesPermission_WRITE_EXTERNAL_STORAGE::class, OUsesPermission_MANAGE_EXTERNAL_STORAGE::class)
    @SuppressLint("MissingPermission")
    private fun applyPermissionStorage(context: Context, onGranted: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_EXTERNAL_STORAGE(context)) {
            onGranted.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_EXTERNAL_STORAGE(context, {
                onGranted.invoke()
            }, {
                XXPermissionsNavHostUtil.startPermission_EXTERNAL_STORAGE(context)
            })
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(OUsesPermission_POST_NOTIFICATIONS::class)
    private fun applyPermissionNotification(context: Context, onGranted: I_Listener) {
        if (XXPermissionsCheckUtil.hasPermission_POST_NOTIFICATIONS(context)) {
            onGranted.invoke()
        } else {
            XXPermissionsRequestUtil.requestPermission_POST_NOTIFICATIONS(context, {
                onGranted.invoke()
            }, {
                XXPermissionsNavHostUtil.startPermission_POST_NOTIFICATIONS(context)
            })
        }
    }

}