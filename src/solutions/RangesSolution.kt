package solutions

import models.Duration
import models.WatchDuration
import helpers.toSeconds
import helpers.toTimestamp
import Solution

class RangesSolution : Solution {
    private var playTimeSeconds: Int = 0
    private var advTimeSeconds: Int = 0
    private var logDurations: List<Duration> = emptyList()


    //합계: 16.1 / 100.0
    override fun solution(play_time: String, adv_time: String, logs: Array<String>): String {
        initValues(play_time, adv_time, logs)

        val watchDurations = createWatchDurations()

        println("RECEIVED WATCH DURATIONS: $watchDurations")
        var highestScoreTime = calculateHighestScoreTime(watchDurations)

        return highestScoreTime.toTimestamp()
    }

    private fun initValues(play_time: String, adv_time: String, logs: Array<String>) {
        playTimeSeconds = play_time.toSeconds()
        advTimeSeconds = adv_time.toSeconds()
        logDurations = logs.map{ log ->
            val section = log.split("-")
            Duration(section[0].toSeconds(), section[1].toSeconds())
        }
    }

    private fun createWatchDurations(): List<WatchDuration> {
        var watchDurations: MutableList<WatchDuration> = mutableListOf()

        logDurations = logDurations.sortedBy { it.startTime }
        var currentViewCount = 0
        var currentStartPointer = 0
        var pastDurationList: MutableList<Duration> = mutableListOf(
            Duration(0, playTimeSeconds)
        )

        for (logDuration in logDurations) {

            //create new sections for ended sections between pointer and next start point
            pastDurationList = pastDurationList.sortedBy { it.endTime }.toMutableList()
            val iter = pastDurationList.listIterator()
            while (iter.hasNext()) {
                val pastDuration = iter.next();
                if (pastDuration.endTime < logDuration.startTime) {
                    val newWatchDuration = WatchDuration(
                        duration = Duration(currentStartPointer, pastDuration.endTime),
                        viewCount = currentViewCount
                    )
                    watchDurations.add(newWatchDuration)
                    currentStartPointer = pastDuration.endTime
                    iter.remove()
                    currentViewCount -= 1
                } else {
                    break;
                }
            }


            //finish section that ends at start of this log
            if (currentStartPointer < logDuration.startTime) {
                val watchDuration = WatchDuration(
                    duration = Duration(currentStartPointer, logDuration.startTime),
                    viewCount = currentViewCount
                )
                watchDurations.add(watchDuration)
            }
            currentStartPointer = logDuration.startTime
            pastDurationList.add(logDuration)
            currentViewCount += 1
        }
        pastDurationList = pastDurationList.sortedBy { it.endTime }.toMutableList()

        val iter = pastDurationList.listIterator()
        while (iter.hasNext()) {
            val pastDuration = iter.next();
            if (currentStartPointer <= pastDuration.endTime) {
                val newWatchDuration = WatchDuration(
                    duration = Duration(currentStartPointer, pastDuration.endTime),
                    viewCount = currentViewCount
                )
                watchDurations.add(newWatchDuration)
                currentStartPointer = pastDuration.endTime
                iter.remove()
                currentViewCount -= 1
            }
        }

        return watchDurations.filter {
            it.totalWatchTime != 0
        }
    }
    private fun calculateHighestScoreTime(watchDurations: List<WatchDuration>): Int {

        val initialScore = calculateScore(watchDurations, Duration(0, advTimeSeconds))

        var highestScore = initialScore
        var highestScoreTime = 0

        var previousScore = initialScore

        val iter = watchDurations.listIterator()
        val currentWatchDurations = mutableListOf<WatchDuration>()

        //create sublist of covered watchDurations as we move along, to save scanning the whole list
        while(iter.hasNext()) {
            val watchDuration = iter.next()
            currentWatchDurations.add(watchDuration)
            if(watchDuration.endTime >= advTimeSeconds) {
                iter.previous()
                break
            }
        }

        val finalStartTime = playTimeSeconds - advTimeSeconds
        for (currentStartTime in 1..finalStartTime) {
            val currentEndTime = currentStartTime + advTimeSeconds

            var newScore = previousScore

            //debugging!!
            if(currentStartTime >= 5458 && currentStartTime <= 5461) {
                val subsesh = getWatchDurationSubsections(
                    watchDurations,
                    Duration(currentStartTime, currentEndTime)
                )
                val subseshvals = subsesh.map {
                    it.calculateScore().toTimestamp()
                }
                val total = subsesh.sumBy { it.calculateScore() }
                val num = 6+6
            }
            //the first watch duration covers the previous first position (before we delete it)
            newScore -= currentWatchDurations.first().viewCount
            newScore += currentWatchDurations.last().viewCount

            //remove first AFTERWARDS because it will
            if(!currentWatchDurations.first().isInRange(currentStartTime)) {
                currentWatchDurations.removeAt(0)
            }
            //simply update the list of covered watch durations whenever it changes any second
            if(!currentWatchDurations.last().isInRange(currentEndTime)) {
                currentWatchDurations.add(iter.next())
            }
            val isHighestScore = newScore > highestScore

            if (isHighestScore) {
                highestScore = newScore
                highestScoreTime = currentStartTime
            }

            previousScore = newScore
        }

        return highestScoreTime
    }

    private fun calculateScore(watchDurations: List<WatchDuration>, duration: Duration): Int {
        val subsections = getWatchDurationSubsections(watchDurations, duration)
        return subsections.sumBy { it.calculateScore() }
    }

    private fun getWatchDurationSubsections(watchDurations: List<WatchDuration>, subSectionDuration: Duration): List<WatchDuration> {
        var watchDurationSubsections: MutableList<WatchDuration> = mutableListOf()

        val iter = watchDurations.listIterator()
        while(iter.hasNext()) {
            val watchDuration = iter.next()
            //find start
            if(watchDuration.endTime >= subSectionDuration.startTime) {
                if(subSectionDuration.endTime < watchDuration.endTime) {
                    //end subsection if end time is within same watch duration
                    watchDurationSubsections.add(
                        WatchDuration(
                            duration = Duration(subSectionDuration.startTime, subSectionDuration.endTime),
                            viewCount = watchDuration.viewCount
                        )
                    )
                    return watchDurationSubsections
                } else {
                    //take the first section and move on to the next loop
                    watchDurationSubsections.add(
                        WatchDuration(
                            duration = Duration(subSectionDuration.startTime, watchDuration.endTime),
                            viewCount = watchDuration.viewCount
                        )
                    )
                }
                break
            }
        }

        while(iter.hasNext()) {
            val watchDuration = iter.next()
            if(watchDuration.endTime < subSectionDuration.endTime) {
                //add middle sections and keep looping while there is remaining time
                watchDurationSubsections.add(
                    WatchDuration(
                        duration = Duration(watchDuration.startTime, watchDuration.endTime),
                        viewCount = watchDuration.viewCount
                    )
                )
            } else {
                //find end (and trim if needed)
                watchDurationSubsections.add(
                    WatchDuration(
                        duration = Duration(watchDuration.startTime, subSectionDuration.endTime),
                        viewCount = watchDuration.viewCount
                    )
                )
                break
            }
        }

        return watchDurationSubsections
    }
}