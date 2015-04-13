package toolbox.analyzers

import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Collections, Comparator, Date}

import akka.actor.ActorSystem
import play.api.Play
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsValue, Json}
import toolbox.Analyzer

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

class ForearmMusclePeakDetector(name: String) extends Analyzer {

    val system = Akka.system(Play.current)

    val displayMovement = true
    val bucketSize = 100
    val bucketTrendsSize = 40
    val bias = 4000.0//6000.0
    val lastValues = new util.ArrayDeque[Double]()
    var lastTrend = "FLAT"
    val doubleComparator = new Comparator[Double]() {
        override def compare(o1: Double, o2: Double): Int = o1.compareTo(o2)
    }

    var lastPeakElevation = 0.0

    var state = "FLAT"
    var peak = new AtomicBoolean(false)

    val hysteresis = new Hysteresis(system)

    override def receiveLineValue(value: Double, n: String, channel: Concurrent.Channel[JsValue]): Unit = {
        if (lastValues.size() > bucketSize) {
            lastValues.pollLast()
        }
        lastValues.push(value)
        val max = Collections.min(lastValues, doubleComparator)
        val min = Collections.max(lastValues, doubleComparator)
        var elevation = Math.abs(max - min) - 2000.0
        if (elevation < 0.0) elevation = 0.0
        if (elevation > 0.0) {
            lastPeakElevation = elevation
        } else {
            if (lastPeakElevation != 0.0) lastPeakElevation = 0.0
        }
        val last = lastValues.take(bucketTrendsSize)
        val acc = (last.last + 1000000) - (last.head + 1000000)
        if (acc > bias && lastTrend != "UP") {
            lastTrend = "UP"
            if (!peak.get()) {
                peak.compareAndSet(false, true)
                hysteresis.on()
                channel.push(Json.obj("peak" -> "on", "name" -> name))
            }
        } else if (acc < -bias && lastTrend != "DOWN") {
            lastTrend = "DOWN"
            if (!peak.get()) {
                peak.compareAndSet(false, true)
                hysteresis.on()
                channel.push(Json.obj("peak" -> "on", "name" -> name))
            }
        } else if (acc > -bias && acc < bias && lastTrend != "FLAT") {
            lastTrend = "FLAT"
            hysteresis.off {
                peak.compareAndSet(true, false)
                channel.push(Json.obj("peak" -> "off", "name" -> name))
            }
        }
    }
}

class Hysteresis(system: ActorSystem) {

    private val scheduled = new AtomicBoolean(false)
    private val more = new AtomicBoolean(false)

    def off(f: => Unit) = {
        def schedule(): Unit = {
            system.scheduler.scheduleOnce(Duration(300, TimeUnit.MILLISECONDS)) {
                if (more.compareAndSet(true, false)) {
                    schedule()
                } else {
                    scheduled.compareAndSet(true, false)
                    f
                }
            }
        }
        if (scheduled.compareAndSet(false, true)) {
            schedule()
        } else {
            more.set(false)
        }
    }

    def on() = {
        more.set(true)
    }
}