package com.mozhimen.installk.splits.ackpine

import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.lifecycle.lifecycleScope
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_POST_NOTIFICATIONS
import com.mozhimen.kotlin.elemk.android.cons.CPermission
import com.mozhimen.kotlin.utilk.android.net.getDisplayName
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.bases.BaseUtilK
import com.mozhimen.kotlin.utilk.wrapper.UtilKPermission
import com.mozhimen.installk.splits.ackpine.cons.SInstallState
import com.mozhimen.installk.splits.ackpine.utils.toUrisList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.splits.Apk
import ru.solrudev.ackpine.splits.ApkSplits.filterCompatible
import ru.solrudev.ackpine.splits.ApkSplits.throwOnInvalidSplitPackage
import ru.solrudev.ackpine.splits.ZippedApkSplits
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.session.ProgressSession
import ru.solrudev.ackpine.session.SessionResult
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.progress

/**
 * @ClassName InstallKSplitsAckpine
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
object InstallKSplitsAckpine : BaseUtilK() {
    @JvmStatic
    @OPermission_POST_NOTIFICATIONS
    @RequiresPermission(CPermission.POST_NOTIFICATIONS)
    fun install(uri: Uri, coroutineScope: CoroutineScope, listener: IA_Listener<SInstallState>? = null) {
        if (!UtilKPermission.hasPostNotification()) {
            UtilKLogWrapper.d(TAG, "install: dont has permission")
            return
        }
        val name = uri.getDisplayName(_context.contentResolver)
        val apks = getApksFromUri(uri, name)
        UtilKLogWrapper.d(TAG, "install: name $name apks $apks")
        installPackage(apks, name, coroutineScope, listener)
    }

    //////////////////////////////////////////////////////////////////////

    private fun getApksFromUri(uri: Uri, name: String): Sequence<Apk> {
        val extension = name.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "apk" -> sequence { Apk.fromUri(uri, _context)?.let { yield(it) } }.constrainOnce()
            "zip", "apks", "xapk", "apkm" -> ZippedApkSplits.getApksForUri(uri, _context)
                .filterCompatible(_context)
                .throwOnInvalidSplitPackage()

            else -> emptySequence()
        }
    }

    private fun installPackage(apks: Sequence<Apk>, fileName: String, coroutineScope: CoroutineScope, listener: IA_Listener<SInstallState>? = null): Job =
        coroutineScope.launch {
            val uris = runInterruptible(Dispatchers.IO) { apks.toUrisList() }
            if (uris.isEmpty()) {
                UtilKLogWrapper.d(TAG, "installPackage: uris is Empty")
                return@launch
            }
            val session = PackageInstaller.getInstance(_context).createSession(uris) {
                name = fileName
                requireUserAction = false
            }
            awaitSession(session, coroutineScope, listener)
        }

    private fun awaitSession(session: ProgressSession<InstallFailure>, coroutineScope: CoroutineScope, listener: IA_Listener<SInstallState>? = null): Job =
        coroutineScope.launch {
            session.progress
                .onEach { progress ->
                    listener?.invoke(SInstallState.Progress(session.id, progress.progress, progress.max))
                    UtilKLogWrapper.d(TAG, "awaitSession: session ${session.id} progress ${progress.progress}") /*sessionDataRepository.updateSessionProgress(session.id, progress)*/
                }
                .launchIn(this)
            try {
                when (val result = session.await()) {
                    is SessionResult.Success -> {
                        listener?.invoke(SInstallState.Success(session.id))
                        UtilKLogWrapper.d(TAG, "awaitSession: ")/*sessionDataRepository.removeSessionData(session.id)*/
                    }

                    is SessionResult.Error -> {
                        listener?.invoke(SInstallState.Fail(session.id, result.cause))
                        UtilKLogWrapper.d(TAG, "awaitSession: ${result.cause}")/*handleSessionError(result.cause.message, session.id)*/
                    }
                }
            } catch (e: Exception) {
//            handleSessionError(e.message, session.id)
                UtilKLogWrapper.e(TAG, "awaitSession ", e)
            }
        }
}