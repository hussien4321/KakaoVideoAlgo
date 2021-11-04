import helpers.toSeconds
import helpers.toTimestamp
import solutions.RangesSolution
import java.util.*

fun selectedSolution(): Solution = RangesSolution()

fun main() {
    println("Solution 1...")
    selectedSolution().solutionWithExpectedResult("02:03:55", "00:14:15", arrayOf("01:20:15-01:45:14", "00:25:50-00:48:29",
        "00:40:31-01:00:00", "01:37:44-02:02:30",
        "01:30:59-01:53:29"), "01:30:59")

    println("Solution 2...")
    selectedSolution().solutionWithExpectedResult("99:59:59", "25:00:00", arrayOf("69:59:59-89:59:59", "01:00:00-21:00:00",
        "79:59:59-99:59:59", "11:00:00-31:00:00"), "01:00:00")


    println("Solution 3...")
    selectedSolution().solutionWithExpectedResult("50:00:00", "50:00:00", arrayOf("15:36:51-38:21:49", "10:14:18-15:36:51",
        "38:21:49-42:51:45"), "00:00:00")

    println("Solution 4...")
    selectedSolution().solutionWithExpectedResult("95:00:00", "70:00:00", arrayOf("00:00:00-24:00:00", "25:00:00-50:00:00",
        "90:00:00-93:00:00", "90:00:00-93:00:00",
        "90:00:00-93:00:00", "90:00:00-93:00:00",
        "90:00:00-93:00:00", "90:00:00-93:00:00",
        "90:00:00-93:00:00", "90:00:00-93:00:00",
        "90:00:00-93:00:00"), "23:00:00")

    val duration = "99:59:59"
    println("Solution 5...")
    selectedSolution().solutionWithTimeMeasurement(duration, "30:00:00", generateDurations(duration, 50000))
}

fun generateDurations(duration: String, logs: Int): Array<String> {
    val durationSeconds = duration.toSeconds()
    return (1..logs).map {
        val randomDuration = Random().nextInt(durationSeconds-1)
        val startTime = Random().nextInt(durationSeconds - randomDuration)
        var endTime = startTime+randomDuration
        "${startTime.toTimestamp()}-${endTime.toTimestamp()}"
    }.toTypedArray()
}