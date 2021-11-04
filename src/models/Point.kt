package models

import helpers.toTimestamp

abstract class Point(val timeStamp: Int, private val type: String) {
    var viewers: Int = 1
    override fun toString(): String = "P: <@${timeStamp.toTimestamp()}, V:$viewers T:$type>"
}

class StartPoint(timeStamp: Int): Point(timeStamp, "START")
class EndPoint(timeStamp: Int): Point(timeStamp, "END")
