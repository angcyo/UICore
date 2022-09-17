/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.angcyo.media.audio.record

import android.os.Environment
import android.os.StatFs
import java.io.File

/**
 * Calculates remaining recording time based on available disk space and
 * optionally a maximum recording file size. The reason why this is not trivial
 * is that the file grows in blocks every few seconds or so, while we want a
 * smooth countdown.
 */
class RemainingTimeCalculator {
    companion object {
        const val UNKNOWN_LIMIT = 0
        const val FILE_SIZE_LIMIT = 1
        const val DISK_SPACE_LIMIT = 2
        private const val EXTERNAL_STORAGE_BLOCK_THREADHOLD = 32
    }

    // which of the two limits we will hit (or have fit) first
    private var mCurrentLowerLimit = UNKNOWN_LIMIT

    // State for tracking file size of recording.
    private var mRecordingFile: File? = null
    private var mMaxBytes: Long = 0

    // Rate at which the file grows
    private var mBytesPerSecond = 0

    // time at which number of free blocks last changed
    private var mBlocksChangedTime: Long = 0

    // number of available blocks at that time
    private var mLastBlocks: Long = 0

    // time at which the size of the file has last changed
    private var mFileSizeChangedTime: Long = 0

    // size of the file at that time
    private var mLastFileSize: Long = 0

    /**
     * If called, the calculator will return the minimum of two estimates: how
     * long until we run out of disk space and how long until the file reaches
     * the specified size.
     *
     * @param file     the file to watch
     * @param maxBytes the limit
     */
    fun setFileSizeLimit(file: File?, maxBytes: Long) {
        mRecordingFile = file
        mMaxBytes = maxBytes
    }

    /**
     * Resets the interpolation.
     */
    fun reset() {
        mCurrentLowerLimit = UNKNOWN_LIMIT
        mBlocksChangedTime = -1
        mFileSizeChangedTime = -1
    }

    /**
     * Returns how long (in seconds) we can continue recording.
     */
    fun timeRemaining(): Long {
        // Calculate how long we can record based on free disk space
        var fs: StatFs? = null
        var blocks: Long = -1
        var blockSize: Long = -1
        val now = System.currentTimeMillis()
        fs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        blocks =
            fs.availableBlocks - EXTERNAL_STORAGE_BLOCK_THREADHOLD.toLong()
        blockSize = fs.blockSize.toLong()
        if (blocks < 0) {
            blocks = 0
        }
        if (mBlocksChangedTime == -1L || blocks != mLastBlocks) {
            mBlocksChangedTime = now
            mLastBlocks = blocks
        }

        /*
         * The calculation below always leaves one free block, since free space
         * in the block we're currently writing to is not added. This last block
         * might get nibbled when we close and flush the file, but we won't run
         * out of disk.
         */

        // at mBlocksChangedTime we had this much time
        var result = mLastBlocks * blockSize / mBytesPerSecond
        // so now we have this much time
        result -= (now - mBlocksChangedTime) / 1000
        if (mRecordingFile == null) {
            mCurrentLowerLimit = DISK_SPACE_LIMIT
            return result
        }

        // If we have a recording file set, we calculate a second estimate
        // based on how long it will take us to reach mMaxBytes.
        mRecordingFile = File(mRecordingFile!!.absolutePath)
        val fileSize = mRecordingFile!!.length()
        if (mFileSizeChangedTime == -1L || fileSize != mLastFileSize) {
            mFileSizeChangedTime = now
            mLastFileSize = fileSize
        }
        var result2 = (mMaxBytes - fileSize) / mBytesPerSecond
        result2 -= (now - mFileSizeChangedTime) / 1000
        result2 -= 1 // just for safety
        mCurrentLowerLimit =
            if (result < result2) DISK_SPACE_LIMIT else FILE_SIZE_LIMIT
        return Math.min(result, result2)
    }

    /**
     * Indicates which limit we will hit (or have hit) first, by returning one
     * of FILE_SIZE_LIMIT or DISK_SPACE_LIMIT or UNKNOWN_LIMIT. We need this to
     * display the correct message to the user when we hit one of the limits.
     */
    fun currentLowerLimit(): Int {
        return mCurrentLowerLimit
    }

    /**
     * Is there any point of trying to start recording?
     */
    fun diskSpaceAvailable(): Boolean {
        val fs =
            StatFs(Environment.getExternalStorageDirectory().absolutePath)
        // keep one free block
        return fs.availableBlocks > EXTERNAL_STORAGE_BLOCK_THREADHOLD
    }

    /**
     * Sets the bit rate used in the interpolation.
     *
     * @param bitRate the bit rate to set in bits/sec.
     */
    fun setBitRate(bitRate: Int) {
        mBytesPerSecond = bitRate / 8
    }
}