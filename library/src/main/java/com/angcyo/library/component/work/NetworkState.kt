package com.angcyo.library.component.work

/**
 * Stores information about network state.
 */
class NetworkState(
    /**
     * Determines if the network is connected. 网络是否连接
     *
     * @return `true` if the network is connected.
     */
    val isConnected: Boolean,
    /**
     * Determines if the network is validated - has a working Internet connection. 网络是否验证过
     *
     * @return `true` if the network is validated.
     */
    val isValidated: Boolean,
    /**
     * Determines if the network is metered. 是否是计量网络
     *
     * @return `true` if the network is metered.
     */
    val isMetered: Boolean,
    /**
     * Determines if the network is not roaming. 是否是非漫游网络
     *
     * @return `true` if the network is not roaming.
     */
    val isNotRoaming: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is NetworkState) {
            return false
        }
        return isConnected == other.isConnected &&
                isValidated == other.isValidated &&
                isMetered == other.isMetered &&
                isNotRoaming == other.isNotRoaming
    }

    override fun hashCode(): Int {
        var result = 0x0000
        if (isConnected) result += 0x0001
        if (isValidated) result += 0x0010
        if (isMetered) result += 0x0100
        if (isNotRoaming) result += 0x1000
        return result
    }

    override fun toString(): String {
        return String.format(
            "[ Connected=%b Validated=%b Metered=%b NotRoaming=%b ]",
            isConnected, isValidated, isMetered, isNotRoaming
        )
    }
}