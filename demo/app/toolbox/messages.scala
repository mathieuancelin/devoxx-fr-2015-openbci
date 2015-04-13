package toolbox

case class Go()

case class Channel(value: Double)

case class Line(content: String)

case class StartReading(path: String)

case class StopReading()

case class Record(
                     index: Int,
                     line1: Double,
                     line2: Double,
                     line3: Double,
                     line4: Double,
                     line5: Double,
                     line6: Double,
                     line7: Double,
                     line8: Double,
                     line9: Double,
                     line10: Double,
                     line11: Double,
                     line12: Double,
                     line13: Double,
                     line14: Double,
                     line15: Double,
                     line16: Double,
                     line17: Double,
                     line18: Double,
                     line19: Double
                     )
