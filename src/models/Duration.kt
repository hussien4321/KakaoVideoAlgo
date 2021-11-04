package models

import helpers.toTimestamp

class Duration(val startTime: Int, var endTime: Int){
    override fun toString(): String = "D:(${startTime.toTimestamp()}, ${endTime.toTimestamp()})"
}
