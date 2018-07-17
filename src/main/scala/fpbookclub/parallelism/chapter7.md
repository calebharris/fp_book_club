# Chapter 7. Purely functional parallelism

## 17 July 2018

Today's goals: exercises 7.5, 7.6, 7.11, 7.12, and 7.13.

### 7.3. Refining the API.

Imagine we want to sort the results of a parallel computation
```scala
def sortPar(parList: Par[List[Int]]): Par[List[Int]]
```
How can we accomplish this? Could run the Par to extract the list, sort it, and repackage in
Par.unit, but that's lame. We want to avoid calling run. What else in our API gives us the
ability to manipulate the result?

Why, `map2`, of course!

```scala
def sortPar(parList: Par[List[Int]]): Par[List[Int]] =
  map2(parList, unit(()))((xs, _) => xs.sorted)
```

We can generalize and "lift" any function `A => B` into the `Par` context, so it becomes
`Par[A] => Par[B]` (i.e. we can map any function over a `Par`).

```scala
def map[A, B](pa: Par[A])(f: A => B): Par[B] =
  map2(pa, unit(()))((a, _) => f(a))
```

Now can rewrite `sortPar`:

```scala
def sortPar(parList: Par[List[Int]]): Par[List[Int]] = map(parList)(_.sorted)
```

That we can implement `map` in terms of `map2` means `map2` is strictly more powerful than `map`.
Now, can we implement map *in parallel*? We can get this far without a new combinator:

```scala
def parMap[A, B](ps: List[A])(f: A => B): Par[List[B]] = {
  val fbs: List[Par[B]] = ps.map(asyncF(f))
  ...
}
```

#### Exercise 7.5

Implement the `sequence` combinator using only existing primitives and without
calling run

```scala
def sequence[A](pas: List[Par[A]]): Par[List[A]]
```

#### Exercise 7.6

Implement `parFilter`

```scala
def parFilter[A](as: List[A])(f: A => Boolean): Par[List[A]]
```

### 7.4. The Alegebra of an API

Can get pretty far just by writing the type signature of a function and "following the types" to an
implementation, sometimes without even thinking of a concrete representation. We are treating the
API as an *algebra*: an abstract set of operations with laws, or properties, we assume to be true.
We've been doing this informally; time to get formal!

#### Law of mapping

```scala
map(unit(1))(_ + 1) == unit(2)
```

What does `==` equal mean for `Par`? Assume it means two `Pars` are equal if their `Futures` hold
the same value *for every valid ExecutionContext argument*. Can generalize the above to:

```scala
map(unit(x))(f) == unit(f(x))
```

This means the law is valid for any choice of `x` and `f`, which implies we cannot make decisions
in our implementation based on the values of either.

Can we reduce to a simpler law? Try substituting the identity function for `f`.

```scala
map( unit(x) )( f  ) == unit(  f(x) )
map( unit(x) )( id ) == unit( id(x) )
map( unit(x) )( id ) == unit( x )
      map( y )( id ) == y             // substitute y for unit(x)
```

And we have a new, simpler law that only talks about `map`. Mapping over any value with the
identity function must result in that value. So `map` can't do a lot of things, such as throw
an exception and crash the computation. It can only apply the function to the result of y.

#### Law of forking

```scala
fork(x) == x
```

Places strong constraints on implementation of fork, because it can't alter the meaning of the
program. The book's implementation causes a deadlock when the thread pool is too small. Ours
doesn't.

### 7.5. Refining combinators to their most general form

Say we want to use the result of one computation to decide which of two others to run.

```scala
def choice[A](cond: Par[Boolean])(t: Par[A], f: Par[A]): Par[A]
```

Would be more useful to support an arbitrary number of choices:

```scala
def choiceN[A](n: Par[Int])(choices: List[Par[A]]): Par[A]
```

#### Exercise 7.11

Implement `choiceN` and then `choice` in terms of `choiceN`.

#### Exercise 7.12

`List` seems arbitrary. What if we have a `Map?` Implement `choiceMap`

```scala
def choiceMap[K, V](key: Par[K])(choices: Map[K, Par[V]]): Par[V]
```

#### Exercise 7.13

Even `Map` is still too specific. We're just using the parts of the `List` and `Map` APIs to
provide a function that maps some value to a Par. So let's implement this function, and then
use it to implement `choice`, `choiceN`, and `choiceMap`.

```scala
def chooser[A, B](pa: Par[A])(choices: A => Par[B]): Par[B]
```

By the way, does this signature look familiar?

## 10 July 2018

Whoops, no notes. We talked through the book's representation of Par, and how ours differs. We also
worked through exercise 7.4, implementing `asyncF` to evaluate any 1-arg function asynchronously.


## 3 July 2018

I'ts been a while! Let's catch up...

1. We're discussing how to represent parallel processing with a functional data structure
2. We're also learning how to design a functional API, in general.
3. Functional parallelism leans heavily into a concept we learned while developing State:
   separating program *description* from program *execution*.
4. We started with the example of calculating the sum of a list in parallel.
5. We talked about issues with using concurrency primitives directly:
   ```scala
   trait Runnable { def run: Unit }

   class Thread(r: Runnable) {
     def start: Unit
     def join: Unit
   }
   ```
   None of the methods on ```Runnable``` or ```Thread``` return a value, meaning to use them we
   have to rely on their side effects, which we want to avoid in the name of referential
   transparency.

   So how about...
   ```scala
   class ExecutorService {
     def submit[A](a: Callable[A]): Future[A]
   }

   trait Future[A] {
     def get: A
   }
   ```
   Better, but still too low level (e.g. Calling ```Future.get``` blocks until a value is ready).
   Also, the API provides no way of composing ```Futures```
6. So we're going to invent our own type: ```Par[A]```, which represents a parallel computation
   resulting in a value of type A.  So far, we've determined we need a few functions.
   ```scala
   // We don't know what Par is yet, so making it a generic type parameter frees us from having
   // to decide prematurely
   trait ParAPI[Par[_]] {
     // Computation that immediately results in the value A
     def unit[A](a: A): Par[A]

     // Combine the results of two parallel computations with a binary function
     def map2[A, B, C](pa: Par[A], pb: Par[B], f: (A, B) => C): Par[C]

     // Marks a computation for concurrent evaluation by run. We talked about whether or not
     // unit should immediately spin off a parallel execution, and decided to give the user of
     // our API the ability to directly control this.
     def fork[A](a: => Par[A]): Par[A]

     // Wraps the expression a for concurrent evaluation by run
     def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

     // Fully evaluates the given Par, spawning concurrent executions as requested by fork,
     // and extracts the resulting value
     def run[A](a: Par[A]): A
   }
   ```
7. So... how can we represent the type ```Par```?

