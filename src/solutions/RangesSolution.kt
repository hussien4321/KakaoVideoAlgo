package solutions

import helpers.toSeconds
import helpers.toTimestamp
import Solution
import helpers.DebuggingTimer
import models.*

class RangesSolution : Solution, DebuggingTimer() {
    private var playTimeSeconds: Int = 0
    private var advTimeSeconds: Int = 0
    private var allWatchDurations: MutableList<WatchDuration> = mutableListOf()
    private val viewingPointsMap = HashMap<Int, Int>()



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

        val viewingPoints = viewingPointsMap.entries.sortedBy { it.key }.filter { it.value != 0 }
        for (viewingPointEntry in viewingPoints) {

            val timeStamp = viewingPointEntry.key
            val viewCount = viewingPointEntry.value

            //End current section at the start of each new point, recording the count at that moment
            if(currentPointer != timeStamp) {
                allWatchDurations.add(WatchDuration(
                    duration = Duration(currentPointer, timeStamp),
                    viewCount = currentViewCount
                ))
            }

            currentViewCount += viewCount

            //update point for next section
            currentPointer = timeStamp
        }
    }


    var startTimePointer = 0
    var endTimePointer = advTimeSeconds

    var startIndex = 0
    var endIndex = 0

    private val currentSubsectionScore: Int get() {
        var score = 0
        for (index in startIndex..endIndex) {
            val watchDuration = allWatchDurations[index]
            var startTime = if(index == startIndex) startTimePointer else watchDuration.startTime
            var endTime = if(index == endIndex) endTimePointer else watchDuration.endTime

            score += (endTime - startTime) * watchDuration.viewCount
        }
        return score
    }

    private fun calculateHighestScoreTime(): Int {
        endTimePointer = advTimeSeconds
        for (index in startIndex.until(allWatchDurations.size)) {
            if(allWatchDurations[index].isInRange(endTimePointer)) {
                endIndex = index
                break
            }
        }

        var bestSubsectionScore = currentSubsectionScore
        var bestSubsectionTime = startTimePointer

        while(endTimePointer != playTimeSeconds) {
            //figure out the minimum jump we can take to observe a different result, skipping intermediate ones
            val timeTillNextSubsectionStart = allWatchDurations[startIndex].endTime - startTimePointer
            val timeTillNextSubsectionEnd = allWatchDurations[endIndex].endTime - endTimePointer

            if(timeTillNextSubsectionStart == 0) startIndex++
            if(timeTillNextSubsectionEnd == 0) endIndex++

            val nextCutAmount = kotlin.math.min(timeTillNextSubsectionStart, timeTillNextSubsectionEnd)

            //trim off the minimum amount from the current subsection
            startTimePointer+=nextCutAmount
            endTimePointer+=nextCutAmount

            updateIndicesToMatchPointers()

            //save new score if higher than previous, if not move on
            val currentSubsectionScore = currentSubsectionScore
            if(currentSubsectionScore > bestSubsectionScore) {
                bestSubsectionScore = currentSubsectionScore
                bestSubsectionTime = startTimePointer
            }
        }

        return bestSubsectionTime
    }

    private fun updateIndicesToMatchPointers() {
        if (startTimePointer > allWatchDurations[startIndex].endTime) {
            startIndex++
            for (index in startIndex.until(allWatchDurations.size)) {
                if (allWatchDurations[index].isInRange(startTimePointer)) {
                    startIndex = index
                    break
                }
            }
        }
        if (endTimePointer > allWatchDurations[endIndex].endTime) {
            endIndex++
            for (index in endIndex.until(allWatchDurations.size)) {
                if(allWatchDurations[index].isInRange(endTimePointer)) {
                    endIndex = index
                    break
                }
            }
        }
    }
}