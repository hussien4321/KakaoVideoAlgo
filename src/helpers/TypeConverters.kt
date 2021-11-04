package helpers

fun String.toSeconds(): Long {
    val hmsStrings = this.split(":")
    val hmsInts = hmsStrings.map{ it.toInt() }
    return hmsInts[0]*3600L + hmsInts[1]*60L + hmsInts[2]
}

fun Long.toTimestamp(): String {
    val timeInSeconds = this
    val hours = timeInSeconds/3600
    var remainingSeconds = timeInSeconds%3600
    val minutes = remainingSeconds/60
    remainingSeconds%=60
    val seconds = remainingSeconds

    return `두자리 문자열로 변환`(hours) + ":" + `두자리 문자열로 변환`(minutes) + ":" + `두자리 문자열로 변환`(seconds)
}


private fun `두자리 문자열로 변환`(number :Long) : String {
    if(number >= 10) {
        return number.toString()
    }
    else {
        return "0"+number.toString()
    }
}