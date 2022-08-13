package com.angcyo.http.form

/**上传的文件信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/13
 */
data class FileUploadInfoBean(
    /**上传成功后的文件id*/
    var fileId: String? = null,
    /**上传的文件本地路径*/
    var filePath: String? = null,
    /**文件可以访问的http地址*/
    var fileUrl: String? = null,
    /**接口返回体字符数据*/
    var responseBody: String? = null,
)
