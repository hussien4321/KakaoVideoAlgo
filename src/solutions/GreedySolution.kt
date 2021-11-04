package solutions

import Solution
import helpers.toSeconds
import helpers.toTimestamp

class GreedySolution : Solution {

    //합계: 54.8 / 100.0
    override fun solution(play_time: String, adv_time: String, logs: Array<String>): String {
        var playTimeSeconds = play_time.toSeconds()
        var advTimeSeconds = adv_time.toSeconds()
        var logTimes: List<Pair<Int,Int>> = logs.map{ log ->
            val section = log.split("-")
            Pair(section[0].toSeconds(), section[1].toSeconds())
        }

        var highestScore = -1
        var highestScoreTime = -1
        for(currentTime in 0..(playTimeSeconds-advTimeSeconds)) {
            val points = `재생시간 계산하기`(currentTime, advTimeSeconds, logTimes)
            if(points > highestScore) {
                highestScore = points
                highestScoreTime = currentTime
            }
        }

        return highestScoreTime.toTimestamp()
    }

    fun `재생시간 계산하기`(currentStartTime: Int, advTime: Int, logs: List<Pair<Int, Int>>): Int {
        var score = 0;
        val currentEndTime = currentStartTime + advTime

        for(log in logs) {
            val overlapStartTime = maxOf(currentStartTime, log.first)
            val overlapEndTime = minOf(currentEndTime, log.second)

            val isOverlapping = overlapStartTime <= overlapEndTime
            if(isOverlapping) {
                score += (overlapEndTime - overlapStartTime) + 1
            }
        }

        return score;
    }
}