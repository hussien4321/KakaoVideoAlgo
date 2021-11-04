package models

import helpers.toTimestamp

abstract class Point(val timeStamp: Long, private val type: String) {
    var viewers: Long = 1
    override fun toString(): String = "P: <@${timeStamp.toTimestamp()}, V:$viewers T:$type>"
}

class StartPoint(timeStamp: Long): Point(timeStamp, "START")
class EndPoint(timeStamp: Long): Point(timeStamp, "END")
