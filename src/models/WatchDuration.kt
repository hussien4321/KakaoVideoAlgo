package models

class WatchDuration(val duration: Duration, val viewCount: Int){
    val startTime: Int get() = duration.startTime
    val endTime: Int get() = duration.endTime

    val totalWatchTime: Int get() = endTime-startTime

    override fun toString(): String = "\n       WD: < $duration, V:$viewCount, L:$totalWatchTime, S:${calculateScore()} >"

    fun calculateScore(): Int = totalWatchTime * viewCount

    fun isInRange(time: Int): Boolean = time in startTime..endTime
}
