package com.angcyo.core.component

import android.content.Context
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import com.angcyo.library.L
import io.reactivex.*

/**
 *
 * 指纹识别
 *
 * [com.angcyo.github.finger.RxFingerPrinter]
 * [com.angcyo.github.biometric.BiometricAuth]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/01/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslFinger {

    /**结果发射器*/
    var _emitter: FlowableEmitter<AuthenticationResult>? = null

    val authenticationCallback = object : FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            super.onAuthenticationError(errMsgId, errString)
            /*if (errMsgId == 10) { //1010
            }*/
            _emitter?.onNext(
                AuthenticationResult(
                    false,
                    AuthenticationException(errMsgId, errString.toString())
                )
            )
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
            super.onAuthenticationHelp(helpMsgId, helpString)
            L.w("helpMsgId:$helpMsgId helpString:$helpString")
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            _emitter?.onNext(AuthenticationResult(true, null))
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            _emitter?.onNext(
                AuthenticationResult(
                    false,
                    AuthenticationException(-1, "onAuthenticationFailed")
                )
            )
        }
    }

    /**开始指纹授权*/
    fun startAuthenticate(context: Context): Single<AuthenticationResult> {
        return Flowable.create<AuthenticationResult>({ emitter ->
            _emitter = emitter
            val fm = FingerprintManagerCompat.from(context)
            when {
                fm.isHardwareDetected.not() -> {
                    _emitter?.onNext(
                        AuthenticationResult(
                            false,
                            AuthenticationException(-1, "无指纹模块")
                        )
                    )
                }
                !fm.hasEnrolledFingerprints() -> {
                    _emitter?.onNext(
                        AuthenticationResult(
                            false,
                            AuthenticationException(-1, "未录入指纹")
                        )
                    )
                }
                else -> {
                    val cancellationSignal = CancellationSignal()
                    fm.authenticate(null, 0, cancellationSignal, authenticationCallback, null)
                }
            }
        }, BackpressureStrategy.LATEST).firstOrError()
    }
}

data class AuthenticationResult(val success: Boolean, val error: AuthenticationException?)
class AuthenticationException(val errMsgId: Int, val errString: String?) :
    RuntimeException(errString)