interface Solution {
    fun solution(play_time: String, adv_time: String, logs: Array<String>): String

    fun solutionWithTimeMeasurement(play_time: String, adv_time: String, logs: Array<String>): String {
        val algorithmStartTime = System.currentTimeMillis()
        val solution = solution(play_time, adv_time, logs)
        val algorithmEndTime = System.currentTimeMillis()
        println("Algorithm Duration: ${algorithmEndTime - algorithmStartTime}ms!")
        return solution
    }

    fun solutionWithExpectedResult(play_time: String, adv_time: String, logs: Array<String>, exp_time: String): String {
        val solution = solutionWithTimeMeasurement(play_time, adv_time, logs)
        if(solution == exp_time) {
            println("PASSED!")
        } else {
            println("FAILED!!! Expected:$exp_time but received $solution")
        }
        return solution
    }
}