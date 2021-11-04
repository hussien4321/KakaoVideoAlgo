package solutions

import helpers.toSeconds
import helpers.toTimestamp
import Solution
import helpers.DebuggingTimer

class RangesSolution : Solution {

    private var playTimeSeconds: Long = 0
    private var advTimeSeconds: Long = 0
    private val viewingPointsMap = HashMap<Long, Long>()
    private val rangesPointsMap = HashMap<Long, Long>()

    override fun solution(play_time: String, adv_time: String, logs: Array<String>): String {
        initValues(play_time, adv_time, logs)

        createWatchDurations()

        var highestScoreTime = calculateHighestScoreTime()

        return highestScoreTime.toTimestamp()
    }

    private fun initValues(play_time: String, adv_time: String, logs: Array<String>) {
        playTimeSeconds = play_time.toSeconds()
        advTimeSeconds = adv_time.toSeconds()
        initViewingPoints(logs)
    }

    private fun initViewingPoints(logs: Array<String>) {
        //We don't need to store start & end points paired together, we only care about HOW MANY people are watching and not WHO,
        // so we only need to know when they start and stop watching.
        logs.forEach { log ->
            val section = log.split("-")

            val startTimestamp = section[0].toSeconds()
            val endTimestamp = section[1].toSeconds()
            viewingPointsMap[startTimestamp] = (viewingPointsMap[startTimestamp]?:0) +1
            viewingPointsMap[endTimestamp] = (viewingPointsMap[endTimestamp]?:0) -1
        }
        //We need to take a section at the end and beginning if there are 0 viewers then.
        viewingPointsMap[playTimeSeconds] = (viewingPointsMap[playTimeSeconds]?:0) -1
    }

    private fun createWatchDurations() {
        var currentViewCount = 0L
        var currentPointer = 0L

        var viewingPoints = viewingPointsMap.entries.sortedBy { it.key }.filter { it.value != 0L }

        for (viewingPointEntry in viewingPoints) {
            val timeStamp = viewingPointEntry.key
            val viewCount = viewingPointEntry.value

            //End current section at the start of each new point, recording the count at that moment
            if(currentPointer != timeStamp) {
                for(i in currentPointer.until(timeStamp)) {
                    rangesPointsMap[i] = currentViewCount
                }
            }

            currentViewCount += viewCount
            //update point for next section
            currentPointer = timeStamp
        }
    }

    private var startTimePointer = 0L
    private val endTimePointer: Long get() = startTimePointer + advTimeSeconds

    private fun calculateHighestScoreTime(): Long {
        var currentScore: Long = (startTimePointer.until(endTimePointer)).map { rangesPointsMap[it]?:0L }.sum()

        var bestSubsectionScore = currentScore
        var bestSubsectionTime = 0L

        for (startTime in 1..playTimeSeconds-advTimeSeconds) {
            val prevStartTime = startTime-1
            val newEndTime = prevStartTime+advTimeSeconds

            currentScore-=rangesPointsMap[prevStartTime]?:0
            currentScore+=rangesPointsMap[newEndTime]?:0

            if(currentScore > bestSubsectionScore) {
                bestSubsectionScore = currentScore
                bestSubsectionTime = startTime
            }

        }

        return bestSubsectionTime
    }
}