package controllers

import akka.actor._
import play.api._
import play.api.libs.EventSource
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc._
import toolbox._
import toolbox.analyzers.{TrendDectector, PeakDetector, ForearmMusclePeakDetector}

object Application extends Controller {

  val system = Akka.system(Play.current)
  val file = "arm-muscle.txt"
  val tailer = system.actorOf(Props(classOf[FileTailer]), IdGenerator.uuid)
  var fileReader = Reference.empty[FileReader]()

  def index = Action {
    Ok(views.html.pdf())
  }

  def game = Action {
    Ok(views.html.game())
  }

  def live = Action {
    tailer ! StartReading(file)
    Ok
  }

  def startReading = Action {
    fileReader.map(_.cancel()).cleanup()
    fileReader <== new FileReader(file)
    fileReader().unqueue()
    Ok
  }

  def stopReading = Action {
    fileReader.map(_.cancel()).cleanup()
    tailer ! StopReading()
    Ok
  }

  def stream = Action {
    val id = IdGenerator.uuid
    val enumerator = Concurrent.unicast[JsValue] { channel =>
      val detector1 = Analyzer("line01", id, _.line1, new TrendDectector, channel)
      val detector2 = Analyzer("line02", id, _.line2, new TrendDectector, channel)
      val detector3 = Analyzer("line03", id, _.line3, new TrendDectector, channel)
      val detector4 = Analyzer("line04", id, _.line4, new TrendDectector, channel)
      val detector5 = Analyzer("line05", id, _.line5, new TrendDectector, channel)
      val detector6 = Analyzer("line06", id, _.line6, new TrendDectector, channel)
      val detector7 = Analyzer("line07", id, _.line7, new TrendDectector, channel)
      val detector8 = Analyzer("line08", id, _.line8, new TrendDectector, channel)
      val actor = system.actorOf(Props(classOf[StreamSubscriber], channel), s"user-stream-${id}")
      actor ! Go()
      detector1 ! Go()
      detector2 ! Go()
      detector3 ! Go()
      detector4 ! Go()
      detector5 ! Go()
      detector6 ! Go()
      detector7 ! Go()
      detector8 ! Go()
    }.onDoneEnumerating {
      system.actorSelection(s"user-stream-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line01-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line02-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line03-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line04-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line05-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line06-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line07-${id}") ! PoisonPill
      system.actorSelection(s"/user/analyzer-line08-${id}") ! PoisonPill
    }
    Ok.feed(enumerator &> EventSource()).as("text/event-stream")
  }
}

