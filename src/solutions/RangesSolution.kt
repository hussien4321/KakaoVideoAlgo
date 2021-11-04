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



    //합계: 71.0 / 100.0 - Simplify point accumulation by accumlating them during creation, to avoid cancelling start/ends (net=0) and overlaying start/ends (net=+>1/-<1)
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



    private fun calculateHighestScoreTime(): Int {
        var currentSubsection = trimAllWatchDurations(advTimeSeconds)

        var bestSubsectionScore = currentSubsection.score
        var bestSubsectionTime = currentSubsection.first().startTime

        var i = 1

        while(allWatchDurations.isNotEmpty()) {
            //figure out the minimum jump we can take to observe a different result, skipping intermediate ones
            val nextCutAmount = kotlin.math.min(currentSubsection.timeTillNextSubsection(), allWatchDurations.timeTillNextSubsection())

            //trim off the minimum amount from the current subsection
            currentSubsection = currentSubsection.split(nextCutAmount).second
            //add the same amount from the remaining watch durations
            currentSubsection.addAll(trimAllWatchDurations(nextCutAmount))

            //save new score if higher than previous, if not move on
            val currentSubsectionScore = currentSubsection.score
            if(currentSubsectionScore > bestSubsectionScore) {
                bestSubsectionScore = currentSubsectionScore
                bestSubsectionTime = currentSubsection.first().startTime
            }
            i++
        }

        return bestSubsectionTime
    }

    private fun trimAllWatchDurations(trimAmount: Int): MutableList<WatchDuration> {
        val allWatchDurationsSplit = allWatchDurations.split(trimAmount)

        allWatchDurations = allWatchDurationsSplit.second
        return allWatchDurationsSplit.first
    }


    private fun MutableList<WatchDuration>.split(splitAmount: Int): Pair<MutableList<WatchDuration>, MutableList<WatchDuration>>{
        var remainingTrimAmount = splitAmount
        var leftWatchDurations: MutableList<WatchDuration> = mutableListOf()
        var rightWatchDurations: MutableList<WatchDuration> = this


        while(remainingTrimAmount > 0) {
            val nextWatchDuration= rightWatchDurations.removeAt(0)

            //check if this entire watch duration can be included in this trimmed subsection
            if(nextWatchDuration.totalWatchTime <= remainingTrimAmount) {
                leftWatchDurations.add(nextWatchDuration)
            }
            //or else we will cut it in 2 and only take part of it
            else {
                var nextWatchDurationSplit = nextWatchDuration.splitWatchDuration(remainingTrimAmount)
                leftWatchDurations.add(nextWatchDurationSplit.first)
                rightWatchDurations.add(0, nextWatchDurationSplit.second)
            }
            remainingTrimAmount -= leftWatchDurations.last().totalWatchTime
        }

        return Pair(leftWatchDurations, rightWatchDurations)
    }


    private fun WatchDuration.splitWatchDuration(splitAmount: Int): Pair<WatchDuration, WatchDuration> {
        return Pair(
            first = WatchDuration(
                Duration(this.startTime, this.startTime + splitAmount), this.viewCount
            ),
            second = WatchDuration(
                Duration(this.startTime + splitAmount, this.endTime), this.viewCount
            )
        )
    }

    private fun List<WatchDuration>.timeTillNextSubsection(): Int = this.first().endTime - this.first().startTime

    private val List<WatchDuration>.score: Int
        get() = this.sumBy { it.calculateScore() }

}