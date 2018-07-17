package fpbookclub.parallelism

import java.util.concurrent.Executors

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/** Functional representation of a parallel execution.
  *
  * The book uses Java primitives and represents a Par as a type alias for the function type
  * java.util.ExecutionService => java.util.Future[A]. We've chosen to go our own route by defining
  * a trait and using the scala.concurrent package, because Scala's concurrency utilities offer a
  * bit more functionality and composability than Java's.
  *
  * @author caleb
  */
trait Par[A] {
  def apply(es: ExecutionContext): Future[A]
}

object Par {

  /** Executes a function asynchronously, wrapping the result in a Future
    */
  def asyncF[A, B](f: A => B): A => Par[B] = a => lazyUnit(f(a))

  /** Returns a new Par that evaluates the given Par asynchronously, assuming the passed-in
    * ExecutionContext supports async execution
    */
  def fork[A](pa: => Par[A]): Par[A] = new Par[A] {
    override def apply(ec: ExecutionContext): Future[A] = {
      implicit val ctx = ec
      Future {
        Await.result(pa(ec), Duration.Inf)
      }
    }
  }

  /** Returns a Par that lazily and asynchronously evaluates the expression a
    */
  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

  /** Returns a Par that results in an immediately-completed Future when applied
    */
  def unit[A](a: A): Par[A] = new Par[A] {
    override def apply(es: ExecutionContext): Future[A] = Future.successful(a)
  }

  /** Returns a Par that wraps the result of a function applied to the result of a Par
    */
  def map[A, B](p: Par[A])(f: A => B): Par[B] =
    map2(p, unit(()))((a, _) => f(a))

  /** Returns a Par that wraps the result of a function applied to the result of two Pars
    */
  def map2[A, B, C](pa: Par[A], pb: Par[B])(f: (A, B) => C): Par[C] =
    new Par[C] {
      override def apply(es: ExecutionContext) = {
        val fa = pa(es)
        val fb = pb(es)
        fa.zipWith(fb)(f)(es)
      }
    }

  /** 'Runs' a Par by applying it to the given ExecutionContext, awaiting the result, and returning
    * it
    */
  def run[A](es: ExecutionContext)(pa: Par[A], timeout: Duration = Duration(30, SECONDS)): A = {
    val f = pa(es)
    Await.result(f, timeout)
  }

  /** Returns a Par wrapping a sort operation */
  def sortPar(parList: Par[List[Int]]): Par[List[Int]] =
    map(parList)(_.sorted)

  /** Returns a Par that calculates the sum of a sequence of integers in parallel
    */
  def sum(ints: IndexedSeq[Int]): Par[Int] =
    if (ints.length <= 1)
      Par.unit(ints.headOption getOrElse 0)
    else {
      val (l, r) = ints.splitAt(ints.length / 2)
      Par.map2(Par.fork(sum(l)), Par.fork(sum(r)))(_ + _)
    }
}
