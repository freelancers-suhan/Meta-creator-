package com.example.service

import com.example.data.model.AccountJobState
import com.example.data.model.LogEntry
import com.example.data.model.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object BatchManager {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isBatchRunning = MutableStateFlow(false)
    val isBatchRunning: StateFlow<Boolean> = _isBatchRunning.asStateFlow()

    private val _cooldownSeconds = MutableStateFlow(0)
    val cooldownSeconds: StateFlow<Int> = _cooldownSeconds.asStateFlow()

    private val _activeJobs = MutableStateFlow<List<AccountJobState>>(emptyList())
    val activeJobs: StateFlow<List<AccountJobState>> = _activeJobs.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private var cooldownJob: Job? = null

    fun setRunning(running: Boolean) {
        _isBatchRunning.value = running
    }

    fun setActiveJobs(jobs: List<AccountJobState>) {
        _activeJobs.value = jobs
    }

    fun updateJob(index: Int, transform: (AccountJobState) -> AccountJobState) {
        _activeJobs.value = _activeJobs.value.mapIndexed { i, job ->
            if (i == index) transform(job) else job
        }
    }

    fun appendLog(index: Int, message: String, type: LogType) {
        _activeJobs.value = _activeJobs.value.mapIndexed { i, job ->
            if (i == index) {
                val newEntry = LogEntry(timestamp = System.currentTimeMillis(), message = message, type = type)
                job.copy(logs = job.logs + newEntry)
            } else job
        }
    }

    fun postSnackbar(message: String) {
        scope.launch {
            _snackbarMessage.emit(message)
        }
    }

    fun startCooldown(seconds: Int = 5) {
        cooldownJob?.cancel()
        _cooldownSeconds.value = seconds
        cooldownJob = scope.launch {
            for (sec in seconds downTo 1) {
                _cooldownSeconds.value = sec
                delay(1000)
            }
            _cooldownSeconds.value = 0
        }
    }

    fun markAllCancelled() {
        _isBatchRunning.value = false
        _activeJobs.value = _activeJobs.value.map { job ->
            if (job.isRunning) {
                job.copy(isRunning = false, isFailed = true, errorMessage = "Stopped by user")
            } else job
        }
    }
}
