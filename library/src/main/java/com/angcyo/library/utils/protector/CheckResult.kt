package com.angcyo.library.utils.protector

class CheckResult(var result: Int, var value: String?) {

    companion object {
        const val RESULT_MAYBE_EMULATOR = 0 //可能是模拟器
        const val RESULT_EMULATOR = 1 //模拟器
        const val RESULT_UNKNOWN = 2 //可能是真机
    }

}