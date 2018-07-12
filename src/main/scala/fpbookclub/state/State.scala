package fpbookclub.state

/**
  * @author caleb
  */
case class State[S, +A](run: S => (A, S)) {

  def runWithLogs(s: S): (A, S) = {
    println(s"Running ${this}")
    run(s)
  }

  def map[B](f: A => B): State[S, B] =
    State(s => {
      val (a, s2) = runWithLogs(s)
      (f(a), s2)
    })

  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State(s => {
      val (a, s2) = runWithLogs(s)
      f(a).runWithLogs(s2)
    })
}

object State {
  def get[S]: State[S, S] = State(s => (s, s))

  def set[S](s: S): State[S, Unit] = State(_ => ((), s))

  def modify[S](f: S => S): State[S, Unit] =
    for {
      s <- get       // Gets the current state and assigns it to `s`.
      _ <- set(f(s)) // Sets the new state to `f` applied to `s`.
    } yield ()

  def unit[S, A](a: A): State[S, A] = State(s => (a, s))

  def sequence[S, A](sas: List[State[S, A]]): State[S, List[A]] = sas match {
    case h :: t => h.flatMap(a => sequence(t).map(as => a :: as))
    case _      => unit[S, List[A]](List())
  }
}

sealed trait Input
case object Coin extends Input
case object Turn extends Input

case class Machine(locked: Boolean, candies: Int, coins: Int)

object Machine {
  val lockedMachine = Machine(true, 10, 0)
  val emptyMachine  = Machine(true, 0, 0)

  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] = inputs match {
    case head :: tail => simulateMachine(head).flatMap(_ => simulateMachine(tail))
    case _            => State(m => ((m.candies, m.coins), m))
  }

  def simulateMachine(input: Input): State[Machine, (Int, Int)] =
    State(
      machine =>
        if (machine.candies <= 0) {
          ((machine.candies, machine.coins), machine)
        } else
          input match {
            case Coin => {
              val m1 = Machine(false, machine.candies, machine.coins + 1)
              ((m1.candies, m1.coins), m1)
            }
            case Turn => {
              if (machine.locked) {
                ((machine.candies, machine.coins), machine)
              } else {
                val m1 = Machine(true, machine.candies - 1, machine.coins)
                ((m1.candies, m1.coins), m1)
              }
            }
          }
    )
}
