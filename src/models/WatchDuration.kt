package models

class WatchDuration(val duration: Duration, val viewCount: Int){
    val startTime: Int = duration.startTime
    val endTime: Int = duration.endTime
    fun isInRange(time: Int): Boolean = time in startTime..endTime

    private val totalWatchTime: Int = endTime-startTime
    val score: Int = totalWatchTime * viewCount
    override fun toString(): String = "\n       WD: < $duration, V:$viewCount, L:$totalWatchTime, S:$score >"
}
