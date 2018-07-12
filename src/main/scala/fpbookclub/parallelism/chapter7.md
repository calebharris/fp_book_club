# Chapter 7. Purely functional parallelism

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
   