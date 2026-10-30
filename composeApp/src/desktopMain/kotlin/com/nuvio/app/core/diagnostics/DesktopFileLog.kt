package com.nuvio.app.core.diagnostics

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import java.io.File
import java.time.LocalTime

/**
 * Writes torrent and warning/error log lines to ~/cinebox-log.txt so playback problems can be
 * diagnosed on machines where the app has no console. The file is recreated at every start.
 */
object DesktopFileLog {
    fun start() {
        runCatching {
            val file = File(System.getProperty("user.home").orEmpty(), "cinebox-log.txt")
            file.writeText("CINEBOX log ${java.time.LocalDateTime.now()}\n")
            Logger.addLogWriter(object : LogWriter() {
                override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                    val interesting = severity >= Severity.Warn ||
                        tag.contains("P2p", ignoreCase = true) ||
                        tag.contains("Torrent", ignoreCase = true) ||
                        tag.contains("Engine", ignoreCase = true)
                    if (!interesting) return
                    runCatching {
                        file.appendText("${LocalTime.now()} ${severity.name.first()}/$tag: $message\n")
                        throwable?.let { file.appendText(it.stackTraceToString().take(1500) + "\n") }
                    }
                }
            })
        }
    }
}
