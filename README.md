# Functional Programming in Scala (as Vistaprint Digital reads it)

Code for and inspired by the exercises in Functional Programming in Scala.

## How to Run

There is no real system here, just a bunch of source files containing answers
to the exercises in the book. Generally, you can play around by launching the
Scala REPL from sbt (obviously, install sbt first):

```
$ sbt
[info] Loading settings from idea.sbt ...
[info] Loading global plugins from ~/.sbt/1.0/plugins
[info] Loading project definition from ~/src/fp_book_club/project
[info] Updating ProjectRef(uri("file:~/src/fp_book_club/project/"), "fp_book_club-build")...
[info] Done updating.
[info] Loading settings from build.sbt ...
[info] Set current project to fp_book_club (in build file:~/src/fp_book_club/)
[info] sbt server started at local://~/.sbt/1.0/server/3c8735a00a155d344a11/sock
sbt:fp_book_club> console
[info] Compiling 1 Scala source to ~/src/fp_book_club/target/scala-2.12/classes ...
[info] Done compiling.
[info] Starting scala interpreter...
Welcome to Scala 2.12.6 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_45).
Type in expressions for evaluation. Or try :help.

scala> import fpbookclub.structures.List
import fpbookclub.structures.List

scala> val l = List(1,2)
l: fpbookclub.structures.List[Int] = Cons(1,Cons(2,Nil))
...
```

## Running Tests

Starting with Chapter 7, in package `fpbookclub.parallelism`, we actually tried
to write tests.  Tests can be run by the `test` command in the sbt shell.  One
can run the tests repeatedly on file changes by using `~test`.
