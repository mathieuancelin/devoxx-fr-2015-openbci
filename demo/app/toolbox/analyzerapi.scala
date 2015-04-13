package toolbox

import akka.actor.{Actor, ActorRef, Props}
import play.api.Play
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue

class AnalyzerActor(lineName: String, extractor: (Record) => Double, analyzer: Analyzer, channel: Concurrent.Channel[JsValue]) extends Actor {
    override def receive: Receive = {
        case Go() =>
            context.system.eventStream.subscribe(self, classOf[Record])
        case Record(index, line1, line2, line3, line4, line5, line6, line7, line8, line9, line10, line11, line12, line13, line14, line15, line16, line17, line18, line19) =>
            lineName match {
                case "line01" => analyzer.receiveLineValue(line1, lineName, channel)
                case "line02" => analyzer.receiveLineValue(line2, lineName, channel)
                case "line03" => analyzer.receiveLineValue(line3, lineName, channel)
                case "line04" => analyzer.receiveLineValue(line4, lineName, channel)
                case "line05" => analyzer.receiveLineValue(line5, lineName, channel)
                case "line06" => analyzer.receiveLineValue(line6, lineName, channel)
                case "line07" => analyzer.receiveLineValue(line7, lineName, channel)
                case "line08" => analyzer.receiveLineValue(line8, lineName, channel)
                case "line09" => analyzer.receiveLineValue(line9, lineName, channel)
                case "line10" => analyzer.receiveLineValue(line10, lineName, channel)
                case "line11" => analyzer.receiveLineValue(line11, lineName, channel)
                case "line12" => analyzer.receiveLineValue(line12, lineName, channel)
                case "line13" => analyzer.receiveLineValue(line13, lineName, channel)
                case "line14" => analyzer.receiveLineValue(line14, lineName, channel)
                case "line15" => analyzer.receiveLineValue(line15, lineName, channel)
                case "line16" => analyzer.receiveLineValue(line16, lineName, channel)
                case _ => 
            }
        case _ =>
    }
}

trait Analyzer {
    def receiveLineValue(value: Double, name: String, channel: Concurrent.Channel[JsValue]): Unit
}

object Analyzer {
    private val system = Akka.system(Play.current)
    def apply[A <: Analyzer](line: String, id: String, extractor: (Record) => Double, analyzer: Analyzer, channel: Concurrent.Channel[JsValue]): ActorRef = {
        system.actorOf(Props(classOf[AnalyzerActor], line, extractor, analyzer, channel), s"analyzer-$line-$id")
    }
}