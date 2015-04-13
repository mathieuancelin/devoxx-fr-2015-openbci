package toolbox.analyzers

import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.{Json, JsValue}
import toolbox.Analyzer

class PeakDetector extends Analyzer {
    val bias = 800.0
    val waitFor = 70
    var last = 0.0
    var peak = false
    var count = 0
    var peakCount = 0
    var first = true
    override def receiveLineValue(value: Double, name: String, channel: Channel[JsValue]): Unit = {
        if (first) {
            first = false
            last = value
        }
        if (peak && count < waitFor) {
            count = count + 1
        } else if (peak && count == waitFor) {
            if (peakCount == 3) {
                peakCount = 0
                count = 0
                peak = false
                channel.push(Json.obj("peak" -> "off", "name" -> name))
            } else if ((last - bias) < value && value < (last + bias)) {
                peak = false
                channel.push(Json.obj("peak" -> "off", "name" -> name))
            } else {
                peakCount = peakCount + 1
            }
            count = 0
        } else {
            count = 0
            if ((last - bias) < value && value < (last + bias)) {
                last = value
            } else {
                peak = true
                channel.push(Json.obj("peak" -> "on", "name" -> name))
            }
        }
    }
}
