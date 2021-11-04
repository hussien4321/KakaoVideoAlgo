package solutions

import helpers.toSeconds
import helpers.toTimestamp
import Solution
import models.*

class RangesSolution2 : Solution {
    private var playTimeSeconds: Int = 0
    private var advTimeSeconds: Int = 0
    private var viewingPoints: MutableList<Point> = mutableListOf()
    private var allWatchDurations: MutableList<WatchDuration> = mutableListOf()

    //합계: 71.0 / 100.0 - Abstracted logs to points, created list of watch durations in L time and loop through taking the nearest end of a section each step
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
            viewingPoints.add(StartPoint(section[0].toSeconds()))
            viewingPoints.add(EndPoint(section[1].toSeconds()))
        }
        //We need to take a section at the end and beginning if there are 0 viewers then.
        viewingPoints.add(EndPoint(playTimeSeconds))
        viewingPoints = viewingPoints.sortedBy { it.timeStamp }.toMutableList()
    }

    private fun createWatchDurations() {
        var currentViewCount = 0
        var currentPointer = 0

        for (viewingPoint in viewingPoints) {

            //End current section at the start of each new point, recording the count at that moment
            if(currentPointer != viewingPoint.timeStamp) {
                allWatchDurations.add(WatchDuration(
                    duration = Duration(currentPointer, viewingPoint.timeStamp),
                    viewCount = currentViewCount
                ))
            }

            //if the new point we discovered is a start point, we know the next section will have +1 count
            if(viewingPoint is StartPoint) {
                currentViewCount++
            } else { //else it must be an end point, so the next section will have a -1 count
              currentViewCount--
            }

            //update point for next section
            currentPointer = viewingPoint.timeStamp
        }
    }



    private fun calculateHighestScoreTime(): Int {
        var currentSubsection = trimWatchDurations(advTimeSeconds)

        var bestSubsectionScore = currentSubsection.score
        var bestSubsectionTime = currentSubsection.first().startTime

        while(allWatchDurations.isNotEmpty()) {
            //figure out the minimum jump we can take to observe a different result, skipping intermediate ones
            val nextCutAmount = kotlin.math.min(currentSubsection.timeTillNextSubsection(), allWatchDurations.timeTillNextSubsection())


            //trim off the minimum amount from the current subsection
            currentSubsection = currentSubsection.split(nextCutAmount).second
            //add the same amount from the remaining watch durations
            currentSubsection.addAll(trimWatchDurations(nextCutAmount))

            //save new score if higher than previous, if not move on
            val currentSubsectionScore = currentSubsection.score
            if(currentSubsectionScore > bestSubsectionScore) {
                bestSubsectionScore = currentSubsectionScore
                bestSubsectionTime = currentSubsection.first().startTime
            }
        }

        return bestSubsectionTime
    }

    private fun trimWatchDurations(trimAmount: Int): MutableList<WatchDuration> {
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