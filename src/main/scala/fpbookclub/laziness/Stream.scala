package fpbookclub.laziness

/**
  * @author caleb
  */
import Stream._

trait Stream[+A] {

  def drop(n: Int): Stream[A] = (n, this) match {
    case (0, _)          => this
    case (_, Cons(_, t)) => t().drop(n - 1)
    case _               => empty
  }

  def exists(p: A => Boolean): Boolean = this match {
    case Cons(h, t) => p(h()) || t().exists(p)
    case _          => false
  }

  def filter(p: A => Boolean): Stream[A] =
    foldRight(empty[A])((a, sa) => if (p(a)) cons(a, sa) else sa)

  def foldRight[B](z: => B)(f: (A, => B) => B): B = this match {
    case Cons(h, t) => {
      println(s"Evaluating h(): ${h()}")
      f(h(), t().foldRight(z)(f))
    }
    case _ => z
  }

  def headOption: Option[A] = this match {
    case Empty      => None
    case Cons(h, t) => Some(h())
  }

  def map[B](f: A => B): Stream[B] = foldRight(empty[B])((a, sb) => cons(f(a), sb))

  def mapViaUnfold[B](f: A => B): Stream[B] = unfold(this) {
    case Cons(h, t) => Some((f(h()), t()))
    case _          => None
  }

  def take(n: Int): Stream[A] = (n, this) match {
    case (0, _)          => empty
    case (_, Cons(h, t)) => cons(h(), t().take(n - 1))
    case _               => empty
  }

  def toList: List[A] = this match {
    case Cons(h, t) => h() :: t().toList
    case Empty      => Nil
  }
}
case object Empty                                   extends Stream[Nothing]
case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]

object Stream {
  def cons[A](hd: => A, tl: => Stream[A]): Stream[A] = {
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)
  }

  def constant[A](a: A): Stream[A] = cons(a, constant(a))

  def from(n: Int): Stream[Int] = cons(n, from(n + 1))

  def empty[A]: Stream[A] = Empty

  def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
    f(z) match {
      case Some((a, s1)) => cons(a, unfold(s1)(f))
      case _             => empty
    }

  def apply[A](as: A*): Stream[A] =
    if (as.isEmpty) empty
    else cons(as.head, apply(as.tail: _*))
}
