package com.vadhod.apkextractor.data.extract

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.IOException
import java.io.OutputStream

/**
 * Writes files into a user-picked SAF tree (persisted via `takePersistableUriPermission`). The
 * constructor validates the tree eagerly so an inaccessible/revoked folder surfaces immediately as an
 * exception the ViewModel turns into a failure result (architecture.md §7.2).
 */
class SafApkSink(private val context: Context, treeUri: Uri) : ApkSink {

    private val root: DocumentFile = DocumentFile.fromTreeUri(context, treeUri)
        ?.takeIf { it.isDirectory && it.canWrite() }
        ?: throw IOException("Export folder is not accessible")

    override fun create(desiredName: String, mimeType: String): SinkFile {
        val name = uniqueName(desiredName)
        val doc = root.createFile(mimeType, name)
            ?: throw IOException("Could not create \"$name\" in the export folder")
        return SafSinkFile(context, doc)
    }

    /** Appends " (n)" before the extension until the name is free, so we never clobber an existing file. */
    private fun uniqueName(name: String): String {
        if (root.findFile(name) == null) return name
        val dot = name.lastIndexOf('.')
        val stem = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var i = 1
        while (root.findFile("$stem ($i)$ext") != null) i++
        return "$stem ($i)$ext"
    }

    private class SafSinkFile(
        private val context: Context,
        private val doc: DocumentFile,
    ) : SinkFile {
        override val displayName: String get() = doc.name ?: "output"

        override fun openOutputStream(): OutputStream =
            context.contentResolver.openOutputStream(doc.uri)
                ?: throw IOException("Could not open output stream for $displayName")

        override fun delete() {
            runCatching { doc.delete() }
        }
    }
}
