package solutions

import Solution
import helpers.toSeconds
import helpers.toTimestamp

class HashmapSolution : Solution {

    //힙계: 58.1 / 100.0
    override fun solution(play_time: String, adv_time: String, logs: Array<String>): String {
        var playTimeSeconds = play_time.toSeconds()
        var advTimeSeconds = adv_time.toSeconds()
        var logTimes: List<Pair<Int,Int>> = logs.map{ log ->
            val section = log.split("-")
            Pair(section[0].toSeconds(), section[1].toSeconds())
        }

        //1. hashmap 만들고
        var timeHashmap : HashMap<Int, Int> = HashMap<Int, Int> ()

        //2. log 위치 마다 어올리는 부분 +1 시키고
        for(log in logTimes) {
            for(logMoment in log.first..log.second) {
                timeHashmap.put(logMoment, (timeHashmap.get(logMoment) ?: 0)+1)
            }
        }

        //3. 위치 1초 올라가면서 지난 구간 점수 기억 하고 새로운 변환 있는지만 확인 하기
        var highestScore = 0
        var highestScoreTime = 0
        for(currentTime in 0..advTimeSeconds) {
            highestScore += timeHashmap.get(currentTime) ?: 0
        }

        var previousScore = highestScore
        for(currentStartTime in 1..playTimeSeconds-advTimeSeconds){
            var currentScore = previousScore
            val currentEndTime = currentStartTime + advTimeSeconds

            currentScore -= timeHashmap.get(currentStartTime-1) ?: 0
            currentScore += timeHashmap.get(currentEndTime) ?: 0

            if(currentScore > highestScore) {
                highestScore = currentScore
                highestScoreTime = currentStartTime
            }

            previousScore = currentScore
        }

        return highestScoreTime.toTimestamp()
    }
}