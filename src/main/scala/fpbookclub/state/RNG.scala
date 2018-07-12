package fpbookclub.state
//
import scala.{Stream => _}
import fpbookclub.laziness.Stream

/**
  * @author caleb
  */
trait RNG {
  def nextInt: (Int, RNG)
}

object RNG {
  // NB - this was called SimpleRNG in the book text

  case class Simple(seed: Long) extends RNG {
    def nextInt: (Int, RNG) = {
      val newSeed = (seed * 0X5DEECE66DL + 0XBL) & 0XFFFFFFFFFFFFL // `&` is bitwise AND. We use the current seed to generate a new seed.
      val nextRNG = Simple(newSeed) // The next state, which is an `RNG` instance created from the new seed.
      val n       = (newSeed >>> 16).toInt // `>>>` is right binary shift with zero fill. The value `n` is our new pseudo-random integer.
      (n, nextRNG) // The return value is a tuple containing both a pseudo-random integer and the next `RNG` state.
    }
  }

  type Rand[+A] = RNG => (A, RNG)

  val int: Rand[Int] = _.nextInt

  def unit[A](a: A): Rand[A] =
    rng => (a, rng)

  def flatMap[A, B](s: Rand[A])(f: A => Rand[B]): Rand[B] =
    rng => {
      val (a, rng2) = s(rng)
      f(a)(rng2)
    }

  def map[A, B](s: Rand[A])(f: A => B): Rand[B] =
    rng => {
      val (a, rng2) = s(rng)
      (f(a), rng2)
    }

  def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
    flatMap(ra)(a => {
      map(rb)(b => f(a, b))
    })

//
//  def both[A,B](ra: Rand[A], rb: Rand[B]): Rand[(A,B)] =
//    map2(ra, rb)((_, _))
//
//  def flatMap[A, B](s: Rand[A])(f: A => Rand[B]): Rand[B] =
//    rng => {
//      val (a, rng2) = s(rng)
//      f(a)(rng2)
//    }
//
//  def nonNegativeEven: Rand[Int] =
//    map(nonNegativeInt)(i => i - i % 2)
//
  def nonNegativeInt(s: Rand[Int]): Rand[Int] = map(int)(i => if (i < 0) -(i + 1) else i)
//
//
////    rng => {
////    val (i, rng2) = rng.nextInt
////    if (i < 0) {
////      (-(i + 1), rng2)
////    } else {
////      (i, rng2)
////    }
////  }
//
//  def double: Rand[Double] = map(nonNegativeInt)(_ / (Int.MaxValue.toDouble + 1))
//
////  def double(rng: RNG): (Double, RNG) = {
////    val (i, r) = nonNegativeInt(rng)
////    (i / (Int.MaxValue.toDouble + 1), r)
////  }
//
//  def intDouble(rng: RNG): ((Int, Double), RNG) = {
//    val (i, rng2) = rng.nextInt
//    val (d, rng3) = double(rng2)
//    ((i, d), rng3)
//  }
//
//  def intDouble2: Rand[(Int, Double)] = flatMap(nonNegativeInt)(i => map(double)(d => (i, d)))
//
//  def doubleInt(rng: RNG): ((Double, Int), RNG) = {
//    val ((i, d), rng2) = intDouble(rng)
//    ((d, i), rng2)
//  }
//
//  def double3(rng: RNG): ((Double, Double, Double), RNG) = {
//    val (d1, rng2) = double(rng)
//    val (d2, rng3) = double(rng2)
//    val (d3, rng4) = double(rng3)
//    ((d1, d2, d3), rng4)
//  }
//
//  def double3AlsoToo: Rand[(Double, Double, Double)] =
//    flatMap(double)(d1 =>
//      flatMap(double)(d2 =>
//        map(double)(d3 =>
//          (d1, d2, d3)
//        )
//      )
//    )
//
//  def ints(count: Int)(rng: RNG): (List[Int], RNG) = {
//    @annotation.tailrec
//    def go(acc: List[Int], rng: RNG, n: Int): (List[Int], RNG) = {
//      if (n <= 0)
//        (acc, rng)
//      else {
//        val (i, rng2) = rng.nextInt
//        go(i :: acc, rng2, n - 1)
//      }
//    }
//    go(List(), rng, count)
//  }
}
