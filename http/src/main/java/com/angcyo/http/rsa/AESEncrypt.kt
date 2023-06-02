package com.angcyo.http.rsa

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @Title: AESEncrypt
 * @Description:
 *
 * 加密工具类
 * @author: liqin
 * @date: 2023/6/1 16:36
 * @Version: 1.0
 */
/**
 * AES256加解密
 */
object AESEncrypt {
    const val UTF_8 = "UTF-8"
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    init {
        try {
            //预热
            Cipher.getInstance(ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }
    }

    /**
     * 生成 SecretKey
     *
     * @param secret
     * @param salt
     * @return
     */
    private fun generateSecretKey(secret: String, salt: String): SecretKey {
        return SecretKeySpec(secret.toByteArray(), "AES")
    }

    /**
     * AES256加密
     *
     * @param content
     * @param secretKey
     * @return
     */
    private fun encryptAES(content: ByteArray, secretKey: SecretKey): ByteArray? {
        var str: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
            str = cipher.doFinal(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return str
    }

    /**
     * AES256解密
     *
     * @param bytes
     * @param secretKey
     * @return
     */
    private fun decryptAES(bytes: ByteArray, secretKey: SecretKey): ByteArray? {
        var decryptStr: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            decryptStr = cipher.doFinal(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return decryptStr
    }

    /**
     * BASE64加密
     *
     * @param content
     * @return
     */
    @Throws(UnsupportedEncodingException::class)
    fun encrypt(content: String, key: String, salt: String): String? {
        val secretKey = generateSecretKey(key, salt)
        val bytes = encryptAES(content.toByteArray(charset(UTF_8)), secretKey) ?: return null
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * BASE64解密
     *
     * @param content
     * @return
     */
    @Throws(UnsupportedEncodingException::class)
    fun decrypt(content: String?, key: String, salt: String): String? {
        val secretKey = generateSecretKey(key, salt)
        return decryptAES(
            Base64.decode(content, Base64.NO_WRAP),
            secretKey
        )?.toString(Charset.defaultCharset())
    }
}

/**使用aes加密*/
fun String.aesEncrypt(key: String, salt: String = "angcyo"): String? =
    AESEncrypt.encrypt(this, key, salt)

/**使用aes解密*/
fun String.aesDecrypt(key: String, salt: String = "angcyo"): String? =
    AESEncrypt.decrypt(this, key, salt)