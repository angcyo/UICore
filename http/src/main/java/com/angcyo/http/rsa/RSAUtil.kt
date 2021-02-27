package com.angcyo.http.rsa

import java.io.ByteArrayOutputStream
import java.security.Key
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

/**
 * RSA Util
 * Author:Bobby
 * DateTime:2019/4/9
 */
object RSAUtil {
    /**
     * encryption algorithm RSA
     */
    const val KEY_ALGORITHM = "RSA"

    /**
     * RSA Maximum Encrypted Plaintext Size
     */
    private const val MAX_ENCRYPT_BLOCK = 117

    /**
     * RSA Maximum decrypted ciphertext size
     */
    private const val MAX_DECRYPT_BLOCK = 256

    fun encrypt(data: String, publicKey: String): String {
        return Base64Util.encode(encrypt(data.toByteArray(), publicKey))
    }

    /**
     * encryption 加密 x509
     * @param data data
     * @param publicKey publicKey
     * @return byte
     * @throws Exception Exception
     */
    @Throws(Exception::class)
    fun encrypt(data: ByteArray, publicKey: String): ByteArray {
        val keyBytes = Base64Util.decode(publicKey)
        val x509KeySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        val publicK: Key = keyFactory.generatePublic(x509KeySpec)
        //val cipher = Cipher.getInstance(keyFactory.algorithm) //后台使用
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")//Android使用
        cipher.init(Cipher.ENCRYPT_MODE, publicK)
        val inputLen = data.size
        val out = ByteArrayOutputStream()
        var offSet = 0
        var cache: ByteArray
        var i = 0
        // Sectional Encryption of Data
        while (inputLen - offSet > 0) {
            cache = if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK)
            } else {
                cipher.doFinal(data, offSet, inputLen - offSet)
            }
            out.write(cache, 0, cache.size)
            i++
            offSet = i * MAX_ENCRYPT_BLOCK
        }
        val encryptedData = out.toByteArray()
        out.close()
        return encryptedData
    }

    fun decrypt(text: String, privateKey: String): String {
        return String(decrypt(Base64Util.decode(text), privateKey), Charsets.UTF_8)
    }

    /**
     * Decrypt 解密 pkcs8
     * @param text text
     * @param privateKey privateKey
     * @return byte
     * @throws Exception Exception
     */
    @Throws(Exception::class)
    fun decrypt(text: ByteArray, privateKey: String): ByteArray {
        val keyBytes = Base64Util.decode(privateKey)
        val pkcs8KeySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        val privateK: Key = keyFactory.generatePrivate(pkcs8KeySpec)
        val cipher = Cipher.getInstance(keyFactory.algorithm)
        cipher.init(Cipher.DECRYPT_MODE, privateK)
        val inputLen = text.size
        val out = ByteArrayOutputStream()
        var offSet = 0
        var cache: ByteArray
        var i = 0
        // Sectional Encryption of Data
        while (inputLen - offSet > 0) {
            cache = if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cipher.doFinal(text, offSet, MAX_DECRYPT_BLOCK)
            } else {
                cipher.doFinal(text, offSet, inputLen - offSet)
            }
            out.write(cache, 0, cache.size)
            i++
            offSet = i * MAX_DECRYPT_BLOCK
        }
        val decryptedData = out.toByteArray()
        out.close()
        return decryptedData
    }
}