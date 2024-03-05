package com.bihe0832.android.lib.utils.encrypt.part

import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
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

    fun splitToDataSegmentList(dataKey: String, longData: ByteArray, chunkSize: Int): List<DataSegment> {
        val dataSize = longData.size
        val numOfChunks = Math.ceil(dataSize.toDouble() / chunkSize).toInt()
        val chunks: MutableList<DataSegment> = ArrayList()
        for (i in 0 until numOfChunks) {
            val start = i * chunkSize
            val length = Math.min(chunkSize, dataSize - start)
            val chunk = ByteArray(length)
            System.arraycopy(longData, start, chunk, 0, length)
            chunks.add(DataSegment(dataKey, start, chunk))
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

    fun splitToDataSegmentList(dataKey: String, longData: String, chunkSize: Int): List<DataSegment> {
        val chunks: MutableList<DataSegment> = ArrayList()
        val data = longData.toByteArray()
        var index = 0
        while (index < longData.length) {
            val partInfo = longData.substring(index, Math.min(index + chunkSize, longData.length))
            chunks.add(DataSegment(dataKey, index, partInfo.toByteArray()))
            index += chunkSize
        }
        return chunks
    }

    fun mergeDataSegment(
        id: String,
        segments: List<DataSegment>,
        totalLength: Int,
        digestType: String?,
        digestValue: String
    ): ByteArray? {
        ZLog.d(TAG, "mergeContent segments size: " + segments.size)
        ZLog.d(TAG, "mergeContent totalLength: $totalLength")
        ZLog.d(TAG, "mergeContent md5: $digestType")
        ZLog.d(TAG, "mergeContent md5: $digestValue")
        val resultSegment = segments.filter { it.dataKey.equals(id) }.distinctBy { it.dataNo }.sortedBy { it.dataNo }
        ZLog.d(TAG, "mergeContent resultSegment size: ${segments.size}")
        // 检查是否收到了所有的片段
        val contentLength = resultSegment.sumOf { it.content.size }
        if (totalLength > 0 && contentLength != totalLength) {
            ZLog.e(TAG, "contentLength is diff with totalLength: $contentLength")
            return null
        } else {
            val data = ByteArray(contentLength)
            for (segment in resultSegment) {
                System.arraycopy(segment.content, 0, data, segment.dataNo, segment.content.size)
            }
            val contentValue = MessageDigestUtils.getDigestData(data, digestType)
            if (!TextUtils.isEmpty(digestType) && TextUtils.isEmpty(digestValue) && !contentValue.equals(digestValue)) {
                ZLog.e(TAG, "content digest is diff with digestValue: $contentValue $digestValue $digestType")
                return null
            } else {
                return data
            }
        }
    }
}
