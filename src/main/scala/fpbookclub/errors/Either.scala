package fpbookclub.errors

/**
  * @author caleb
  */
sealed trait Either[+E, +A] {
  def map[B](f: A => B): Either[E, B] = this match {
    case Left(e)  => Left(e)
    case Right(a) => Right(f(a))
  }
}

case class Left[+E](value: E) extends Either[E, Nothing] {
  override def map[B](f: Nothing => B): Either[E, B] = this
}

case class Right[+A](value: A) extends Either[Nothing, A] {
  override def map[B](f: A => B): Either[Nothing, B] = Right(f(value))
}

object Either {
  def left[E](value: E): Either[E, Nothing] = Left(value)

  def right[A](value: A): Either[Nothing, A] = Right(value)
}
