package fpbookclub.errors

import scala.{Option => _}

/**
  * @author caleb
  */
sealed trait Option[+A] {

  def map[B](f: A => B): Option[B] = flatMap(a => Some(f(a)))

  def flatMap[B](f: A => Option[B]): Option[B] = this match {
    case None    => None
    case Some(x) => f(x)
  }

  def getOrElse[B >: A](default: => B): B = this match {
    case None    => default
    case Some(x) => x
  }

  def orElse[B >: A](ob: => Option[B]): Option[B] = this map (a => Some(a)) getOrElse ob

  def filter(f: A => Boolean): Option[A] = this.flatMap(a => if (f(a)) Some(a) else None)

}

case class Some[+A](get: A) extends Option[A]
case object None            extends Option[Nothing]

object Option {
  def some[A](a: A): Option[A] = Some(a)

  def none[A]: Option[A] = None

  def map2[A, B, C](oa: Option[A], ob: Option[B])(f: (A, B) => C): Option[C] =
    oa flatMap { a =>
      ob map { f(a, _) }
    }

  def map2For[A, B, C](oa: Option[A], ob: Option[B])(f: (A, B) => C): Option[C] =
    for {
      a <- oa
      b <- ob
    } yield f(a, b)

  def mean(ds: Seq[Double]): Option[Double] =
    if (ds.isEmpty)
      None
    else
      Some(ds.sum / ds.size)

//  def variance(ds: Seq[Double]): Option[Double] = {
//    for {
//      m <- mean(ds)
//      x <- ds
//      l <- math.pow(x - m, 2)
//    } yield mean(l)
//  }

  def varianceExpanded(ds: Seq[Double]): Option[Double] = {
    mean(ds).flatMap(
      m =>
        mean(ds.map { x =>
          math.pow(x - m, 2)
        }).map(v => v)
    )
  }

  def sequence[A](os: List[Option[A]]): Option[List[A]] =
    os.foldLeft(Some(List()): Option[List[A]])(
      (ola, oa) =>
        map2(ola, oa) { (as, a) =>
          as :+ a
        }
    )
}
