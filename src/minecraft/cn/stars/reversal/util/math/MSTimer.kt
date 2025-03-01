package cn.stars.reversal.util.math

class MSTimer {
    private var time = -1L

    fun hasTimePassed(MS: Long): Boolean {
        return System.currentTimeMillis() >= time + MS
    }

    fun hasTimeLeft(MS: Long): Long {
        return MS + time - System.currentTimeMillis()
    }


    fun timePassed(): Long {
        return System.currentTimeMillis() - time
    }

    fun reset() {
        time = System.currentTimeMillis()
    }
}