package com.mozhimen.installk.splits.ackpine.test

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.mozhimen.kotlin.elemk.androidx.lifecycle.bases.BaseViewModel
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import ru.solrudev.ackpine.exceptions.ConflictingBaseApkException
import ru.solrudev.ackpine.exceptions.ConflictingPackageNameException
import ru.solrudev.ackpine.exceptions.ConflictingSplitNameException
import ru.solrudev.ackpine.exceptions.ConflictingVersionCodeException
import ru.solrudev.ackpine.exceptions.NoBaseApkException
import ru.solrudev.ackpine.exceptions.SplitPackageException
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.installer.getSession
import ru.solrudev.ackpine.session.ProgressSession
import ru.solrudev.ackpine.session.SessionResult
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.parameters.NotificationString
import ru.solrudev.ackpine.session.progress
import ru.solrudev.ackpine.splits.Apk
import java.util.UUID

/**
 * @ClassName MainViewModel
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
class MainViewModel(
    private val packageInstaller: PackageInstaller,
    private val sessionDataRepository: SessionDataRepository
) : BaseViewModel() {

    init {
        viewModelScope.launch {
            val sessions = sessionDataRepository.sessions.value
            if (sessions.isNotEmpty()) {
                sessions
                    .map { sessionData ->
                        async { packageInstaller.getSession(sessionData.id) }
                    }
                    .awaitAll()
                    .filterNotNull()
                    .forEach(::awaitSession)
            }
        }
    }

    private val error = MutableStateFlow(NotificationString.empty())
    val uiState = combine(
        error,
        sessionDataRepository.sessions,
        sessionDataRepository.sessionsProgress,
        ::InstallUiState
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), InstallUiState())


    fun installPackage(apks: Sequence<Apk>, fileName: String) = viewModelScope.launch {
        val uris = runInterruptible(Dispatchers.IO) { apks.toUrisList() }
        if (uris.isEmpty()) {
            UtilKLogWrapper.d(TAG, "installPackage: uris is Empty")
            return@launch
        }
        val session = packageInstaller.createSession(uris) {
            name = fileName
            requireUserAction = false
        }
        val sessionData = SessionData(session.id, fileName)
        sessionDataRepository.addSessionData(sessionData)
        awaitSession(session)
    }

    private fun awaitSession(session: ProgressSession<InstallFailure>) = viewModelScope.launch {
        session.progress
            .onEach { progress -> sessionDataRepository.updateSessionProgress(session.id, progress) }
            .launchIn(this)
        try {
            when (val result = session.await()) {
                is SessionResult.Success -> sessionDataRepository.removeSessionData(session.id)
                is SessionResult.Error -> handleSessionError(result.cause.message, session.id)
            }
        } catch (exception: CancellationException) {
            sessionDataRepository.removeSessionData(session.id)
            throw exception
        } catch (e: Exception) {
            handleSessionError(e.message, session.id)
            Log.e("InstallViewModel", null, e)
        }
    }

    private fun handleSessionError(message: String?, sessionId: UUID) {
        val error = if (message != null) {
            NotificationString.resource(R.string.session_error_with_reason, message)
        } else {
            NotificationString.resource(R.string.session_error)
        }
        sessionDataRepository.setError(sessionId, error)
    }

    ///////////////////////////////////////////////////////////////////

    fun cancelSession(id: UUID) = viewModelScope.launch {
        packageInstaller.getSession(id)?.cancel()
    }

    fun removeSession(id: UUID) = sessionDataRepository.removeSessionData(id)

    fun clearError() {
        error.value = NotificationString.empty()
    }

    private fun Sequence<Apk>.toUrisList(): List<Uri> {
        try {
            return map { it.uri }.toList()
        } catch (exception: SplitPackageException) {
            val errorString = when (exception) {
                is NoBaseApkException -> NotificationString.resource(R.string.error_no_base_apk)
                is ConflictingBaseApkException -> NotificationString.resource(R.string.error_conflicting_base_apk)
                is ConflictingSplitNameException -> NotificationString.resource(
                    R.string.error_conflicting_split_name,
                    exception.name
                )

                is ConflictingPackageNameException -> NotificationString.resource(
                    R.string.error_conflicting_package_name,
                    exception.expected, exception.actual, exception.name
                )

                is ConflictingVersionCodeException -> NotificationString.resource(
                    R.string.error_conflicting_version_code,
                    exception.expected, exception.actual, exception.name
                )
            }
            error.value = errorString
            return emptyList()
        } catch (exception: Exception) {
            error.value = NotificationString.raw(exception.message.orEmpty())
            return emptyList()
        }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[APPLICATION_KEY]!!
                val packageInstaller = PackageInstaller.getInstance(application)
                val savedStateHandle = extras.createSavedStateHandle()
                val sessionsRepository = SessionDataRepositoryImpl(savedStateHandle)
                return MainViewModel(packageInstaller, sessionsRepository) as T
            }
        }
    }
}