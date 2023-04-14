package com.angcyo.library.component

/**
 * 用来执行ping命令
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/14
 */
object Ping {

    /**ping返回结果*/
    data class PingResult(
        var result: Int = 0,
        var command: String? = null,//ping -c 4 -w 1000 www.baidu.com
        var host: String? = null,
        var count: Int = 0,
        var timeout: Int = 0,
        /**ping命令返回值*/
        var output: String? = null
    )

    /**
     * 执行ping命令
     * @param host 目标主机
     * @param count ping次数
     * @param timeout 超时时间
     * @param listener 回调
     */
    fun ping(
        host: String,
        count: Int = 1,
        timeout: Int = 1000,
        listener: (PingResult) -> Unit
    ) {
        try {
            val command = "ping -c $count -w $timeout $host"
            val process = Runtime.getRuntime().exec(command)
            val result = process.waitFor()
            val pingResult = PingResult()
            pingResult.result = result
            pingResult.command = command
            pingResult.host = host
            pingResult.count = count
            pingResult.timeout = timeout

            //PING 127.0.0.1 (127.0.0.1) 56(84) bytes of data.
            //64 bytes from 127.0.0.1: icmp_seq=1 ttl=64 time=0.241 ms
            //64 bytes from 127.0.0.1: icmp_seq=2 ttl=64 time=0.621 ms
            //64 bytes from 127.0.0.1: icmp_seq=3 ttl=64 time=0.464 ms
            //64 bytes from 127.0.0.1: icmp_seq=4 ttl=64 time=0.861 ms
            //
            //--- 127.0.0.1 ping statistics ---
            //4 packets transmitted, 4 received, 0% packet loss, time 3062ms
            //rtt min/avg/max/mdev = 0.241/0.546/0.861/0.227 ms

            /*PING www.a.shifen.com (14.119.104.254) 56(84) bytes of data.
                64 bytes from 14.119.104.254: icmp_seq=1 ttl=56 time=12.3 ms
                64 bytes from 14.119.104.254: icmp_seq=2 ttl=56 time=26.8 ms
                64 bytes from 14.119.104.254: icmp_seq=3 ttl=56 time=31.9 ms
                64 bytes from 14.119.104.254: icmp_seq=4 ttl=56 time=25.7 ms

                --- www.a.shifen.com ping statistics ---
                        4 packets transmitted, 4 received, 0% packet loss, time 3007ms
                rtt min/avg/max/mdev = 12.312/24.229/31.983/7.271 ms*/

            pingResult.output = process.inputStream.bufferedReader().readText()
            listener(pingResult)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}