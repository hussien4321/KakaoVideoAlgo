package solutions

import Solution
import helpers.toSeconds
import helpers.toTimestamp

class GreedySolution : Solution {

    //합계: 54.8 / 100.0
    override fun solution(play_time: String, adv_time: String, logs: Array<String>): String {
        var playTimeSeconds = play_time.toSeconds()
        var advTimeSeconds = adv_time.toSeconds()
        var logTimes: List<Pair<Long,Long>> = logs.map{ log ->
            val section = log.split("-")
            Pair(section[0].toSeconds(), section[1].toSeconds())
        }

        var highestScore = -1L
        var highestScoreTime = -1L
        for(currentTime in 0..(playTimeSeconds-advTimeSeconds)) {
            val points = `재생시간 계산하기`(currentTime, advTimeSeconds, logTimes)
            if(points > highestScore) {
                highestScore = points
                highestScoreTime = currentTime
            }
        }

        return highestScoreTime.toTimestamp()
    }

    fun `재생시간 계산하기`(currentStartTime: Long, advTime: Long, logs: List<Pair<Long, Long>>): Long {
        var score = 0L
        val currentEndTime = currentStartTime + advTime

        for(log in logs) {
            val overlapStartTime = maxOf(currentStartTime, log.first)
            val overlapEndTime = minOf(currentEndTime, log.second)

            val isOverlapping = overlapStartTime <= overlapEndTime
            if(isOverlapping) {
                score += (overlapEndTime - overlapStartTime) + 1L
            }
        }

        return score
    }
}