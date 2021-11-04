package models

import helpers.toTimestamp

class Duration(val startTime: Long, var endTime: Long){
    override fun toString(): String = "D:(${startTime.toTimestamp()}, ${endTime.toTimestamp()})"
}
