package com.aetherglass.data.mapper

import com.aetherglass.data.api.ProcessInfoResponse
import com.aetherglass.domain.model.ProcessInfo
import com.aetherglass.domain.model.ProcessStatus

object ProcessMapper {

    fun toDomain(response: ProcessInfoResponse): ProcessInfo {
        return ProcessInfo(
            pid = response.pid,
            name = response.name,
            cpuPercent = response.cpuPercent,
            memoryBytes = response.memoryBytes,
            memoryPercent = response.memoryPercent,
            status = ProcessStatus.values().firstOrNull { it.name.equals(response.status, ignoreCase = true) }
                ?: ProcessStatus.Unknown,
            user = response.user,
            startTime = response.startTime,
            isSystem = response.isSystem,
            isProtected = response.isProtected,
            commandLine = response.commandLine,
            executablePath = response.executablePath
        )
    }
}