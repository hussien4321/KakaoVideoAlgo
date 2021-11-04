package models

class WatchDuration(val duration: Duration, val viewCount: Int){
    val startTime: Long = duration.startTime
    val endTime: Long= duration.endTime
    fun isInRange(time: Long): Boolean = time in startTime..endTime

    private val totalWatchTime: Long = endTime-startTime
    val score: Long = totalWatchTime * viewCount
    override fun toString(): String = "\n       WD: < $duration, V:$viewCount, L:$totalWatchTime, S:$score >"
}
