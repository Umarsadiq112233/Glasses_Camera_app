package com.oudmon.wifi.bean

class DownloadSpeedCalculator {
    // 保存最近一次计算状态
    private var lastBytes: Long = 0
    private var lastTime: Long = -1

    fun start() {
        lastTime = System.currentTimeMillis()
        lastBytes = 0
    }


    fun calculate(currentBytes: Long): String {
        val now = System.currentTimeMillis()
        if (lastTime == -1L) {
            return "-1"
        }
        val elapsedMs = (now - lastTime).coerceAtLeast(1)
        val deltaBytes = currentBytes - lastBytes
        val speedBps = deltaBytes * 1000.0 / elapsedMs // 准确字节/秒

        // 更新状态
        lastTime = now
        lastBytes = currentBytes
        return formatSpeed(speedBps)
    }

    private fun formatSpeed(speedBps:Double):String {
       return when {
            speedBps >= 1_048_576 -> "%.1f MB/s".format(speedBps / 1_048_576)
            speedBps >= 1024 -> "%.1f KB/s".format(speedBps / 1024)
            speedBps < 1 -> "0 B/s"
            else -> "%.0f B/s".format(speedBps)
        }
    }
}
