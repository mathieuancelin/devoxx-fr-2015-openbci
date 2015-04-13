package toolbox

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import akka.actor.Actor
import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}

import scala.util.{Failure, Success, Try}

class FileTailer extends Actor {

    var tailer: Tailer = _

    def toDouble(v: String): Double = v.replace(",", ".").toDouble

    // %First Column = SampleIndex
    // %Other Columns = EEG data in microvolts followed by Accel Data (in G) interleaved with Aux Data
    // 0, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, 0,00, 0,00, 0,00, 0,00, 0,00, 0,00, 0,00, 0,00, -0,01, 0,00, 0,50
    // 1, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, -187500,02, 0,00, 0,00, 0,00, 0,00, 0,00, 0,00, 0,00, 0,00, -0,01, 0,00, 0,50
    override def receive: Receive = {
        case StartReading(path) =>
            tailer = Tailer.create(new File(path), new TailerListenerAdapter {
                override def handle(line: String): Unit = self ! Line(line)
            }, 4, true)
            context.dispatcher.execute(tailer)
        case StopReading() =>
            if (tailer != null) tailer.stop()
        case Line(content) =>
            content.split(", ").toList match {
                case index :: line1 :: line2 :: line3 :: line4 :: line5 :: line6 :: line7 :: line8 :: line9 :: line10 :: line11 :: line12 :: line13 :: line14 :: line15 :: line16 :: line17 :: line18 :: line19 :: tail =>
                    Try(Record(index.toInt, toDouble(line1), toDouble(line2), toDouble(line3), toDouble(line4), toDouble(line5), toDouble(line6), toDouble(line7), toDouble(line8), toDouble(line9), toDouble(line10), toDouble(line11), toDouble(line12), toDouble(line13), toDouble(line14), toDouble(line15), toDouble(line16), toDouble(line17), toDouble(line18), toDouble(line19))) match {
                        case Success(record) => context.system.eventStream.publish(record)
                        case Failure(e) =>
                    }
                case index :: line1 :: line2 :: line3 :: line4 :: line5 :: line6 :: line7 :: line8 :: line9 :: line10 :: line11 :: tail =>
                    Try(Record(index.toInt, toDouble(line1), toDouble(line2), toDouble(line3), toDouble(line4), toDouble(line5), toDouble(line6), toDouble(line7), toDouble(line8), toDouble(line9), toDouble(line10), toDouble(line11), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)) match {
                        case Success(record) => context.system.eventStream.publish(record)
                        case Failure(e) =>
                    }
                case _ => //println(s"bad format '$content'")
            }
        case _ =>
    }
}
