package toolbox

import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.{TimeUnit, ConcurrentLinkedQueue}
import java.util.concurrent.atomic.AtomicLong

import akka.actor.Cancellable
import com.google.common.io.Files
import play.api.Play
import play.api.libs.concurrent.Akka

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class FileReader(filePath: String) {

    private def toDouble(v: String): Double = v.replace(",", ".").toDouble

    private val system = Akka.system(Play.current)
    private val lines = Files.readLines(new File(filePath), Charset.forName("UTF-8"))
    private val queue = new ConcurrentLinkedQueue[String](lines.toList)
    private val inc = new AtomicLong(0L)
    private var cancellable: Cancellable = _

    def unqueue(): Unit = {
        val line = queue.poll()
        line.split(", ").toList match {
            case index :: line1 :: line2 :: line3 :: line4 :: line5 :: line6 :: line7 :: line8 :: line9 :: line10 :: line11 :: line12 :: line13 :: line14 :: line15 :: line16 :: line17 :: line18 :: line19 :: tail =>
                Try(Record(index.toInt, toDouble(line1), toDouble(line2), toDouble(line3), toDouble(line4), toDouble(line5), toDouble(line6), toDouble(line7), toDouble(line8), toDouble(line9), toDouble(line10), toDouble(line11), toDouble(line12), toDouble(line13), toDouble(line14), toDouble(line15), toDouble(line16), toDouble(line17), toDouble(line18), toDouble(line19))) match {
                    case Success(record) => system.eventStream.publish(record)
                    case Failure(e) =>
                }
            case index :: line1 :: line2 :: line3 :: line4 :: line5 :: line6 :: line7 :: line8 :: line9 :: line10 :: line11 :: tail =>
                Try(Record(index.toInt, toDouble(line1), toDouble(line2), toDouble(line3), toDouble(line4), toDouble(line5), toDouble(line6), toDouble(line7), toDouble(line8), toDouble(line9), toDouble(line10), toDouble(line11), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)) match {
                    case Success(record) => system.eventStream.publish(record)
                    case Failure(e) =>
                }
            case _ => println(s"[READER] bad format $line")
        }
        cancellable = system.scheduler.scheduleOnce(Duration(1, TimeUnit.MILLISECONDS))(unqueue())
    }

    def cancel() = {
        if (cancellable != null) {
            cancellable.cancel()
        }
    }
}
