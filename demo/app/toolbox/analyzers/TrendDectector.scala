package toolbox.analyzers

import java.util

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.{JsValue, Json}
import toolbox.Analyzer
import collection.JavaConversions._

class TrendDectector extends Analyzer {

    val bucketSize = 50
    val values = new util.ArrayDeque[Double](bucketSize)
    var peak = true
    var up = true

    override def receiveLineValue(value: Double, name: String, channel: Channel[JsValue]): Unit = {
        if (values.size() > bucketSize) {
            values.pollLast()
        }
        values.push(value)
        if (values.size() >= bucketSize) {
            var direction = "DOWN"
            var ya = values.toList(0)
            var yb = values.toList(bucketSize - 1)
            if (ya > yb) {
                direction = "UP"
                val tmp = ya
                ya = yb
                yb = tmp
            }
            val xa = 0.0
            val xb = bucketSize.toDouble
            val distance = Math.sqrt(((xb - xa) * (xb - xa)) + ((yb - ya) * (yb - ya)))
            val angle = Math.toDegrees(Math.acos(bucketSize.toDouble / distance))
            if (angle > 88.0) {
                if (!peak) {
                    peak = true
                    if (up && direction == "DOWN") {
                        up = false
                        Logger.info(s"    $direction at ${new DateTime().toString("HH:mm:ss:SSS")}")
                        Logger.info("}")
                    }
                    if (!up && direction == "UP") {
                        up = true
                        Logger.info(s"PEAK on $name {")
                        Logger.info(s"    $direction   at ${new DateTime().toString("HH:mm:ss:SSS")}")
                    }
                    channel.push(Json.obj("peak" -> "on", "name" -> name))
                }
            } else {
                if (peak) {
                    peak = false
                    channel.push(Json.obj("peak" -> "off", "name" -> name))
                }
            }
        }
    }
 }
