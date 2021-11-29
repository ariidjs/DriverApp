package app.proyekakhir.core.data.source.remote.network

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream


class ProgressRequestBody(private val mFile: File,
                          private val mListener: UploadCallbacks,
                          private val content_type: String,
                            private val name: String): RequestBody() {

    override fun contentType(): MediaType? {
        return content_type.toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return mFile.length()
    }

    override fun writeTo(sink: BufferedSink) {
        val fileLength = mFile.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(mFile)
        var uploaded: Long = 0

        fileInputStream.use { it ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (it.read(buffer).also { read = it } != -1) {

                // update progress on UI thread
                handler.post(ProgressUpdater(uploaded, fileLength))
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        }
    }

    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int, name: String)
        fun onError()
        fun onFinish()
    }

    inner class ProgressUpdater(private val mUploaded: Long, private val mTotal: Long) :
        Runnable {
        override fun run() {
            mListener.onProgressUpdate((100 * mUploaded / mTotal).toInt(), name)
        }
    }
}