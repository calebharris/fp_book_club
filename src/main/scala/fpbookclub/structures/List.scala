package fpbookclub.structures

sealed trait List[+A]
case object Nil                             extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List {

  def append[A](a1: List[A], a2: List[A]): List[A] =
    a1 match {
      case Nil        => a2
      case Cons(h, t) => Cons(h, append(t, a2))
    }

  def appendViaFold[A](a1: List[A], a2: List[A]): List[A] =
    foldLeft(a2, a1)((a, b) => setHead(a, b))

  def appendViaFoldLeft[A](a1: List[A], a2: List[A]): List[A] =
    foldLeft(reverse(a1), a2)((y, x) => Cons(x, y))

  def drop[A](l: List[A], n: Int): List[A] = n match {
    case 0 => l
    case _ =>
      l match {
        case Nil         => Nil
        case Cons(_, xs) => drop(xs, n - 1)
      }
  }

  def tail(l: List[_]): List[_] = drop(l, 1)

  def dropWhile[A](l: List[A])(f: A => Boolean): List[A] = l match {
    case Nil         => Nil
    case Cons(x, xs) => if (f(x)) dropWhile(xs)(f) else l
  }

  def init[A](l: List[A]): List[A] = l match {
    case Nil          => Nil
    case Cons(x, Nil) => Nil
    case Cons(x, xs)  => Cons(x, init(xs))
  }

  def setHead[A](l: List[A], e: A): List[A] = l match {
    case Nil         => Cons(e, Nil)
    case Cons(_, xs) => Cons(e, xs)
  }

//  def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B = as match {
//    case Cons(x, xs) => f(x, foldRight(xs, z)(f))
//    case _ => z
//  }

  def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B = {
    foldLeft[A, B](reverse(as), z)((b, a) => f(a, b))
  }

  @annotation.tailrec
  def foldLeft[A, B](as: List[A], z: B)(f: (B, A) => B): B = as match {
    case Nil         => z
    case Cons(x, xs) => foldLeft(xs, f(z, x))(f)
  }

  def sum(ns: List[Int]): Int = foldRight(ns, 0)(_ + _)

  def sum2(ns: List[Int]): Int = foldLeft(ns, 0)(_ + _)

  def product(ns: List[Double]): Double = foldRight(ns, 1.0)(_ * _)

  def product2(ns: List[Double]): Double = foldLeft(ns, 1.0)(_ * _)

  def length[A](l: List[A]): Int = foldRight(l, 0)((_, b) => 1 + b)

  def length2[A](l: List[A]): Int = foldLeft(l, 0)((b, _) => 1 + b)

  def reverse[A](l: List[A]): List[A] = foldLeft(l, List[A]())((as, x) => Cons(x, as))

  def incrementAll(l: List[Int]): List[Int] = l match {
    case Cons(x, xs) => Cons(x + 1, incrementAll(xs))
    case _           => Nil
  }

//  def map[A, B](l: List[A])(f: A => B): List[B] = l match {
//    case Cons(x, xs) => Cons(f(x), map(xs)(f))
//    case _ => Nil
//  }

  def map[A, B](l: List[A])(f: A => B): List[B] = flatMap(l)(a => List(f(a)))

  def mapViaFold[A, B](l: List[A])(f: A => B): List[B] =
    foldRight(l, List[B]())((h, t) => Cons(f(h), t))

  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  def flatMap[A, B](as: List[A])(f: A => List[B]): List[B] = as match {
    case Cons(x, xs) => append(f(x), flatMap(xs)(f))
    case _           => Nil
  }

  def filter[A](as: List[A])(p: A => Boolean): List[A] =
    flatMap(as)(a => if (p(a)) List(a) else Nil)
}
