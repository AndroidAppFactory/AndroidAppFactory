package com.bihe0832.android.lib.audio.wav

import com.bihe0832.android.lib.utils.ConvertUtils
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.math.floor

class WavFileReader {

    private var mFileSize: Long = 0

    private var mChunkId: String = ""
    private var mChunkSize: Int = 0
    private var mFormat: String = ""
    private var mSuChunkId_1: String = ""
    private var mSuChunkSize_1: Int = 0
    private var mAudioFormat: Int = 0
    private var mNumChannels: Int = 0
    private var mSampleRate: Int = 0
    private var mByteRate: Int = 0
    private var mBlockAlign: Int = 0
    private var mBitPerSample: Int = 0
    private var mExtraParamSize: Int = 0
    private var mExtraParam: Int = 0
    private var mSuChunkId_2: String = ""
    private var mSuChunkSize_2: Int = 0


    fun parseWavFileHeader(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            return
        }

        mFileSize = File(filePath).length()
        try {
            readHead(DataInputStream(FileInputStream(filePath)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getAudioLength(): Long {
        val audioDataSize = mFileSize - 44
        val bytePerSample = 2 * mNumChannels
        val totalSample = audioDataSize / bytePerSample
        val durationInMillis = (totalSample / (mSampleRate * mNumChannels).toFloat()) * 1000

        return floor(durationInMillis).toLong()
    }


    private fun readHead(mDataInputStream: DataInputStream) {

        mChunkId = readString(mDataInputStream, 4)
        mChunkSize = readInt(mDataInputStream) ?: 0
        mFormat = readString(mDataInputStream, 4)
        mSuChunkId_1 = readString(mDataInputStream, 4)
        mSuChunkSize_1 = readInt(mDataInputStream) ?: 0
        mAudioFormat = readShort(mDataInputStream).toInt()
        mNumChannels = readShort(mDataInputStream).toInt()
        mSampleRate = readInt(mDataInputStream)
        mByteRate = readInt(mDataInputStream)
        mBlockAlign = readShort(mDataInputStream).toInt()
        mBitPerSample = readShort(mDataInputStream).toInt()
        mSuChunkId_2 = readString(mDataInputStream, 4)
        mSuChunkSize_2 = readInt(mDataInputStream)

//        ZLog.d(TAG, "ChunkId -> $mChunkId")
//        ZLog.d(TAG, "ChunkSize -> $mChunkSize")
//        ZLog.d(TAG, "Format -> $mFormat")
//        ZLog.d(TAG, "SubChunkId_1 -> $mSuChunkId_1")
//        ZLog.d(TAG, "SubChunkSize_1 -> $mSuChunkSize_1")
//        ZLog.d(TAG, "AudioFormat -> $mAudioFormat")
//        ZLog.d(TAG, "NumChannels -> $mNumChannels")
//        ZLog.d(TAG, "SampleRate -> $mSampleRate")
//        ZLog.d(TAG, "ByteRate -> $mByteRate")
//        ZLog.d(TAG, "BlockAlign -> $mBlockAlign")
//        ZLog.d(TAG, "BitPerSample -> $mBitPerSample")
//        ZLog.d(TAG, "SuChunkId_2 -> $mSuChunkId_2")
//        ZLog.d(TAG, "SuChunkSize_2 -> $mSuChunkSize_2")

    }

    private fun readString(mDataInputStream: DataInputStream, length: Int): String {
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(readChar(mDataInputStream))
        }
        return sb.toString()
    }

    private fun readChar(mDataInputStream: DataInputStream): Char {
        return mDataInputStream.readByte().toInt().toChar()
    }

    private fun readInt(mDataInputStream: DataInputStream): Int {
        val byteArray = ByteArray(4)
        mDataInputStream.read(byteArray)
        return ConvertUtils.bytesToInt(byteArray)
    }

    private fun readShort(mDataInputStream: DataInputStream): Short {
        val byteArray = ByteArray(2)
        mDataInputStream.read(byteArray)
        return ConvertUtils.bytesToShort(byteArray)
    }

}