package com.bihe0832.android.lib.utils.encrypt.part

import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MessageDigestUtils

/**
 * Summary
 *
 * @author code@bihe0832.com
 * Created on 2024/3/5.
 * Description:
 */
object DataSegmentTools {

    private val TAG = "DataSegmentTools"

    fun splitToByteArrayList(longData: ByteArray, chunkSize: Int): List<ByteArray> {
        val dataSize = longData.size
        val numOfChunks = Math.ceil(dataSize.toDouble() / chunkSize).toInt()
        val chunks: MutableList<ByteArray> = ArrayList(numOfChunks)
        for (i in 0 until numOfChunks) {
            val start = i * chunkSize
            val length = Math.min(chunkSize, dataSize - start)
            val chunk = ByteArray(length)
            System.arraycopy(longData, start, chunk, 0, length)
            chunks.add(chunk)
        }
        return chunks
    }

    fun splitToDataSegmentList(longData: ByteArray, chunkSize: Int, digestType: String?): List<DataSegment> {
        val dataSize = longData.size
        val numOfChunks = Math.ceil(dataSize.toDouble() / chunkSize).toInt()
        val dataSig = MessageDigestUtils.getDigestData(longData, digestType)
        val chunks: MutableList<DataSegment> = ArrayList()
        for (i in 0 until numOfChunks) {
            val start = i * chunkSize
            val length = Math.min(chunkSize, dataSize - start)
            val chunk = ByteArray(length)
            System.arraycopy(longData, start, chunk, 0, length)
            chunks.add(DataSegment(start, chunk, dataSize, dataSig))
        }
        return chunks
    }

    fun splitToStringList(longData: String, chunkSize: Int): List<String> {
        val dataSlices: MutableList<String> = ArrayList()
        var index = 0
        while (index < longData.length) {
            dataSlices.add(longData.substring(index, Math.min(index + chunkSize, longData.length)))
            index += chunkSize
        }
        return dataSlices
    }

    fun splitToDataSegmentList(longData: String, chunkSize: Int, digestType: String?): List<DataSegment> {
        val chunks: MutableList<DataSegment> = ArrayList()
        val data = longData.toByteArray()
        val length = data.size
        val dataSig = MessageDigestUtils.getDigestData(data, digestType)
        var index = 0
        while (index < longData.length) {
            val partInfo = longData.substring(index, Math.min(index + chunkSize, longData.length))
            chunks.add(DataSegment(index, partInfo.toByteArray(), length, dataSig))
            index += chunkSize
        }
        return chunks
    }

    fun mergeDataSegment(segments: List<DataSegment>, totalLength: Int, md5: String): ByteArray? {
        ZLog.d(TAG, "mergeContent segments size: " + segments.size)
        ZLog.d(TAG, "mergeContent totalLength: $totalLength")
        ZLog.d(TAG, "mergeContent md5: $md5")
        if (TextUtils.isEmpty(md5 as CharSequence)) {
            ZLog.e(TAG, "parseDataSegmentList bad md5 is empty ")
            return null
        } else {
            val resultSegment =
                segments.filter { it.signatureValue.equals(md5) }.distinctBy { it.start }.sortedBy { it.start }
            ZLog.d(TAG, "mergeContent resultSegment size: ${segments.size}")
            // 检查是否收到了所有的片段
            val contentLength = resultSegment.sumOf { it.content.size }
            return if (contentLength == totalLength) {
                val data = ByteArray(contentLength)
                for (segment in resultSegment) {
                    System.arraycopy(segment.content, 0, data, segment.start, segment.content.size)
                }
                if (MD5.getMd5(data).equals(md5)) {
                    data
                } else {
                    ZLog.e(TAG, "content md5 is diff with md5: ${MD5.getMd5(data)}")
                    null
                }
            } else {
                ZLog.e(TAG, "contentLength is diff with totalLength: $contentLength")
                null
            }
        }
    }
}
