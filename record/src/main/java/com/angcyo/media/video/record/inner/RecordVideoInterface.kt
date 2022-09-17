package com.angcyo.media.video.record.inner

/**
 * Created by dalong on 2017/1/3.
 */
interface RecordVideoInterface {
    /**
     * 开始录制
     */
    fun startRecord()

    /**
     * 正在录制
     * @param recordTime 录制的时间
     */
    fun onRecording(recordTime: Long)

    /**
     * 录制完成
     * @param videoPath  录制保存的路径
     */
    fun onRecordFinish(videoPath: String)

    /**
     * 录制出问题
     */
    fun onRecordError()

    /**
     * 拍照
     */
    fun onTakePhoto(data: ByteArray)
}