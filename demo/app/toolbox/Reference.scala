package toolbox

import java.util.concurrent.atomic.AtomicReference

class Reference[T](name: String) {

  private val ref = new AtomicReference[Option[T]](None)

  // TODO : only settable once ???
  def <==(value: T): Option[T] = set(value)

  def set(value: T): Option[T] = ref.getAndSet(Option(value))

  def cleanup(): Option[T] = ref.getAndSet(None)

  def apply(): T = ref.get().getOrElse(throw new RuntimeException(s"Reference to $name was not properly initialized ..."))

  def get() = apply()

  def asOption() = ref.get()

  private def withValue(value: Option[T]): Reference[T] = {
    val newRef = Reference.empty[T]()
    newRef.ref.set(value)
    newRef
  }

  def ==>(f: (T) => Any): Reference[T] = combine(f)
  def mutate(f: (T) => Any): Reference[T] = combine(f)
  def chain(f: (T) => Any): Reference[T] = combine(f)
  def combine(f: (T) => Any): Reference[T] = {
    f(get())
    this
  }

  def isEmpty: Boolean = ref.get.isEmpty
  def isDefined: Boolean = ref.get.isDefined
  def getOrElse[B >: T](default: => B): B = ref.get.getOrElse(default)
  def map[B](f: (T) => B): Reference[B] = new Reference[B](name).withValue(ref.get.map(f))
  def fold[B](ifEmpty: => B)(f: (T) => B): B = ref.get.fold(ifEmpty)(f)
  def flatMap[B](f: (T) => Reference[B]): Reference[B] = new Reference[B](name).withValue(ref.get.flatMap(t => f(t).asOption()))
  def filter(p: (T) => Boolean): Reference[T] = new Reference[T](name).withValue(ref.get.filter(p))
  def filterNot(p: (T) => Boolean): Reference[T] = new Reference[T](name).withValue(ref.get.filterNot(p))
  def nonEmpty: Boolean = ref.get.nonEmpty
  def exists(p: (T) => Boolean): Boolean = ref.get.exists(p)
  def forall(p: (T) => Boolean): Boolean = ref.get.forall(p)
  def foreach[U](f: (T) => U): Unit = ref.get.foreach(f)
  def call[U](f: (T) => U): Unit = ref.get.foreach(f)
  def collect[B](pf: PartialFunction[T, B]): Reference[B] = new Reference[B](name).withValue(ref.get.collect(pf))
  def orElse[B >: T](alternative: => Reference[B]): Reference[B] = new Reference[B](name).withValue(ref.get.orElse(alternative.asOption()))
}

object Reference {
  def of[T](value: T): Reference[T]               = apply(value)
  def of[T](name: String, value: T): Reference[T] = apply(name, value)
  def apply[T](value: T): Reference[T]               = apply(IdGenerator.uuid, value)
  def apply[T](name: String, value: T): Reference[T] = {
    val ref = new Reference[T](name)
    ref.set(value)
    ref
  }
  def empty[T](name: String): Reference[T] = new Reference[T](name)
  def empty[T](): Reference[T] = empty(IdGenerator.uuid)
}

import java.util.concurrent.atomic.AtomicLong
import scala.util.Random

object IdGenerator {

  private[this] val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray.map(_.toString)
  private[this] val EXTENDED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789*$£%)([]!=+-_:/;.><&".toCharArray.map(_.toString)
  private[this] val INIT_STRING = for(i <- 0 to 15) yield Integer.toHexString(i)

  val generatorId = Reference.apply(1L)
  private[this] val minus = 1288834974657L
  private[this] val counter = new AtomicLong(-1L)
  private[this] val lastTimestamp = new AtomicLong(-1L)

  if (generatorId.getOrElse(1L) > 1024L) throw new RuntimeException("Generator id can't be larger than 1024")

  def nextId() = synchronized {
    val timestamp = System.currentTimeMillis
    if (timestamp < lastTimestamp.get()) throw new RuntimeException("Clock is running backward. Sorry :-(")
    lastTimestamp.set(timestamp)
    counter.compareAndSet(4095, -1L)
    ((timestamp - minus) << 22L) | (generatorId.getOrElse(1L) << 10L) | counter.incrementAndGet()
  }

  def uuid: String = (for {
    c <- 0 to 36
  } yield c match {
      case i if i == 9 || i == 14 || i == 19 || i == 24 => "-"
      case i if i == 15 => "4"
      case i if c == 20 => INIT_STRING((Random.nextDouble() * 4.0).toInt | 8)
      case i => INIT_STRING((Random.nextDouble() * 15.0).toInt | 0)
    }).mkString("")

  def token(characters: Array[String], size: Int): String = (for {
    i <- 0 to size - 1
  } yield characters(Random.nextInt(characters.size))).mkString("")

  def token(size: Int): String = token(CHARACTERS, size)
  def token: String = token(64)
  def extendedToken(size: Int): String = token(EXTENDED_CHARACTERS, size)
  def extendedToken: String = token(EXTENDED_CHARACTERS, 64)
}