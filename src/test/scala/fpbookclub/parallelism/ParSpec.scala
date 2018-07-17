package fpbookclub.parallelism

import org.scalatest.{Assertions, BeforeAndAfterEach, FlatSpec, Matchers, Suite}
import java.util.concurrent.{Executors, ThreadPoolExecutor, TimeoutException}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.concurrent.duration._

import Assertions._
import ThreadPoolExecutor._

trait Executor extends BeforeAndAfterEach { this: Suite =>
  private val defaultTaskCount: () => Long = () => -1

  var executionContext: ExecutionContextExecutorService = null
  var taskCount                                         = defaultTaskCount

  override def beforeEach = {
    val executorService = Executors.newFixedThreadPool(10)
    executorService match {
      case x: ThreadPoolExecutor => {
        // We don't care if a task gets submitted to a shut down executor
        x.setRejectedExecutionHandler(new DiscardPolicy())

        // Create way to interrogate number of tasks executed asynchronously
        taskCount = () => x.getTaskCount
      }
    }
    executionContext = ExecutionContext.fromExecutorService(executorService)
    super.beforeEach
  }

  override def afterEach = {
    try super.afterEach
    finally {
      executionContext.shutdown
      taskCount = defaultTaskCount
    }
  }
}

class ParSpec extends FlatSpec with Matchers with Executor {

  // Produce a Par that tries to wait millis milliseconds before returning value
  def sleepAndReturn[A](millis: Long, value: A) = Par.lazyUnit {
    try {
      Thread.sleep(millis)
    } catch {
      case e: InterruptedException => ()
    }
    value
  }

  "Par.map" should "apply the function to the argument" in {
    val p = Par.unit("what is my length?")
    assertResult(18) {
      Par.run(executionContext)(Par.map(p)(_.length))
    }
  }

  "Par.map2" should "apply the function to both arguments" in {
    val pa = Par.unit("hello")
    val pb = Par.unit(3)
    assertResult("hellohellohello") {
      Par.run(executionContext)(Par.map2(pa, pb)(_ * _))
    }
  }

  "Par.unit" should "return the value synchronously" in {
    assertResult((6, 0)) {
      (Par.run(executionContext)(Par.unit(6)), taskCount())
    }
  }

  "Par.run" should "throw an exception on timeout" in {
    val p  = sleepAndReturn[Int](1000, 1)
    val p2 = sleepAndReturn[Int](1000, 2)
    val m2 = Par.fork(Par.map2(p, p2) { _ + _ })

    assertThrows[TimeoutException] {
      val x = Par.run(executionContext)(m2, Duration(500, MILLISECONDS))
    }
  }

  "Par.asyncF" should "return immediately and evaluate asynchronously" in {
    val f = (a: Int) => {
      Thread.sleep(1000)
      a * 2
    }

    val t0 = System.currentTimeMillis
    val x  = Par.asyncF(f)(10)
    val t1 = (System.currentTimeMillis - t0)
    assert(t1 < 1000L)

    val b  = Par.run(executionContext)(x)
    val t2 = System.currentTimeMillis - t0
    assert(t2 >= 1000)
    assertResult(1) {
      taskCount()
    }
  }

  "Par.sortPar" should "sort the list" in {
    assertResult(List(1, 2, 3, 4, 5)) {
      Par.run(executionContext)(Par.sortPar(Par.unit(List(3, 2, 5, 1, 4))))
    }
  }

  "Par.sum" should "calculate the sum in parallel" in {
    val v   = Vector(1, 2, 3, 4)
    val sum = Par.run(executionContext)(Par.sum(v))
    assertResult(10)(sum)
    assert(taskCount() >= 2, ", at least 2 tasks should have been submitted")
  }
}
