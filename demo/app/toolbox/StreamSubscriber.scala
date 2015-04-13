package toolbox

import java.util.concurrent.atomic.AtomicLong

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{Json, JsValue}

class StreamSubscriber(channel: Concurrent.Channel[JsValue]) extends Actor {

    val inc = new AtomicLong(0L)

    var lastTimestamp = System.currentTimeMillis()
    var waiting = Json.arr()

    override def receive: Receive = {
        case Go() =>
            context.system.eventStream.subscribe(self, classOf[Record])
        case Record(index, line1, line2, line3, line4, line5, line6, line7, line8, line9, line10, line11, line12, line13, line14, line15, line16, line17, line18, line19) =>
            val timestamp = System.currentTimeMillis()
            waiting = waiting.append(Json.obj(
                "timestamp" -> timestamp,
                "line01" -> line1,
                "line02" -> line2,
                "line03" -> line3,
                "line04" -> line4,
                "line05" -> line5,
                "line06" -> line6,
                "line07" -> line7,
                "line08" -> line8,
                "line09" -> line9,
                "line10" -> line10,
                "line11" -> line11,
                "line12" -> line12,
                "line13" -> line13,
                "line14" -> line14,
                "line15" -> line15,
                "line16" -> line16,
                "line17" -> line17,
                "line18" -> line18,
                "line19" -> line19
            ))
            if (timestamp > (lastTimestamp + 100)) {
                lastTimestamp = timestamp
                //Logger.info(s"Sent at $lastTimestamp : ${waiting.value.size}")
                channel.push(waiting)
                waiting = Json.arr()
            }
        case _ =>
    }
}