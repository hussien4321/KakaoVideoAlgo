package solutions

import helpers.toSeconds
import helpers.toTimestamp
import Solution
import helpers.DebuggingTimer
import java.util.Map

class RangesSolution : Solution, DebuggingTimer() {
    private var playTimeSeconds: Int = 0
    private var advTimeSeconds: Int = 0
    private val viewingPointsMap = HashMap<Int, Int>()
    private val rangesPointsMap = HashMap<Int, Int>()



    override fun solution(play_time: String, adv_time: String, logs: Array<String>): String {
        initValues(play_time, adv_time, logs)

        startDebuggingTimer()
        createWatchDurations()
        stopDebuggingTimer("createWatchDurations()")

        var highestScoreTime = calculateHighestScoreTime()
        stopDebuggingTimer("calculateHighestScoreTime()")

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
        var currentViewCount = 0
        var currentPointer = 0

        var viewingPoints = viewingPointsMap.entries.sortedBy { it.key }.filter { it.value != 0 }

        for (viewingPointEntry in viewingPoints) {

            val timeStamp = viewingPointEntry.key
            val viewCount = viewingPointEntry.value

            //End current section at the start of each new point, recording the count at that moment
            if(currentPointer != timeStamp) {
                for(i in currentPointer..timeStamp) {
                    rangesPointsMap[i] = currentViewCount
                }
            }

            currentViewCount += viewCount
            //update point for next section
            currentPointer = timeStamp
        }
    }


    var startTimePointer = 0
    private val endTimePointer: Int get() = startTimePointer + advTimeSeconds


    private fun calculateHighestScoreTime(): Int {
        var currentScore = (startTimePointer.until(endTimePointer)).map{
            rangesPointsMap[it]  ?: 0
        }.sumBy { it }

        var bestSubsectionScore = currentScore
        var bestSubsectionTime = startTimePointer

        while(endTimePointer <= playTimeSeconds) {

            currentScore-=rangesPointsMap[startTimePointer]?:0
            currentScore+=rangesPointsMap[endTimePointer]?:0

            startTimePointer++

            if(currentScore > bestSubsectionScore) {
                bestSubsectionScore = currentScore
                bestSubsectionTime = startTimePointer
            }

        }

        return bestSubsectionTime
    }
}