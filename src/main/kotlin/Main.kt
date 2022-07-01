
import kotlinx.coroutines.*
import kotlinx.coroutines.async
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("Contributors")

fun log(msg: String?) {
   log.info(msg)
}

fun main(args: Array<String>) {
    //suspendFunctionsPractice()
    //log.info(Thread.currentThread().name.toString()) Main thread
    callingMultipleCoroutines()
//    runBlocking {
//        val value = coroutineWithContextReturns()
//        println("Value returned from withcontext is : $value")
//    }

    //coroutineAsyncAwaitReturns()
}

private fun coroutineAsyncAwaitReturns(): Int{
    val value: Int
    runBlocking { // code inside coroutine scope executes sequentially

        println("1")
        val result = async {
            println("2")
            coroutineWithContextReturns()
        }

        println("3")
        log.info("")

        value = result.await()
        result.join()
        // the code below it is waiting until the result await gives value. its like join
        println(" value from async $value")

        println("4")

    }
    println("5")
    return value

    // Actual result
    //1
    //3
    //16:57:46.452 [main @coroutine#1] INFO Contributors -
    //2  // since 2 belongs to inner scope, its flow is different from parent scope
    //code inside with context before delay
    //16:57:46.473 [DefaultDispatcher-worker-1 @coroutine#2] INFO Contributors - inside with context
    //code inside with context after delay
    //code outside with context
    // value from async 5
    //4
    //5
}
private suspend fun coroutineWithContextReturns(): Int {

    //Suspend function 'withContext' should be called only from a coroutine or another suspend function
    val value = withContext(Dispatchers.Default) {  // withcontext can be called without any outer coroutine scope
        // whereas async should be called within outer scope.
        println("code inside with context before delay")
        log.info("inside with context")
        delay(2000)
        println("code inside with context after delay")
        5
    }

    println("code outside with context")

    return value

    // Actual output
    //code inside with context before delay
    //15:47:53.760 [DefaultDispatcher-worker-1 @coroutine#1] INFO Contributors - inside with context
    //code inside with context after delay
    //code outside with context

    // OBSERVATION
    // with context works like launch and join, i.e once this block is executed, then only next is proceeded.
}

// Dispatchers default
//The default CoroutineDispatcher that is used by all standard builders like launch, async, etc.
//It is backed by a shared pool of threads on JVM. By default, the maximal level of parallelism used
// by this dispatcher is equal to the number of CPU cores, but is at least two. Level of parallelism X
// guarantees that no more than X tasks can be executed in this dispatcher in parallel.

// Dispatchers Main
//A coroutine dispatcher that is confined to the Main thread operating with UI objects.
// This dispatcher can be used either directly or via MainScope factory.
// Usually such dispatcher is single-threaded.
//Access to this property may throw IllegalStateException if no main thread dispatchers are present in the classpath.

// Dispatchers IO
// It defaults to the limit of 64 threads or the number of cores (whichever is larger).
//designed for offloading blocking IO tasks to a shared pool of threads.

// Dispatchers.Unconfined
//is not confined to any specific thread. It executes initial continuation of the coroutine in the current call-frame
// and lets the coroutine resume in whatever thread that is used by the corresponding suspending function,
// without mandating any specific threading policy. Nested coroutines launched in this dispatcher form an
// event-loop to avoid stack overflows.

private fun callingMultipleCoroutines(){
    val startTime = System.currentTimeMillis()
    runBlocking(Dispatchers.Unconfined) {

        val jobs = List(100000){
            launch {
                log.info("inside launch coroutine no: $it")
                delay(1000)
                //println(it)
            }// .join()  // this .join() makes this coroutine to be executed completely before starting new coroutine
        }

        println("Called before 100000 coroutines are created")
        log.info("outside launch ")

//        jobs.forEach{
//            println("check")
//            it.join()
//        }

        val job = jobs[0]

    }
    val currentTime = System.currentTimeMillis()

    println("Time duration is : ${currentTime - startTime}")
}


// We create a coroutine using coroutine scope builders like launch, runblocking, async/await, withcontext
private fun suspendFunctionsPractice(){
    runBlocking {
        launch {  // should be called from other coroutine scope
            // this whole scope is coroutine, the below inside this scope are executed sequentially
            computationIntensiveTask()
            println("inside coroutine scope")

        }

        // since the above function is suspended, the thread was set free so the below line executed
        println("inside run blocking, check whether this is executed before launch scope")
    }

    println("Yes run blocking blocks the thread, this execution performed after the blocking scope is executed")
}

suspend fun computationIntensiveTask(){
    delay(5000)  //Suspend function 'delay' should be called only from a coroutine or another suspend function
    println("task execution after 5 seconds")
}

private fun x(){
    runBlocking {
        delay(4)  // delay is suspend function
    }

    runBlocking {
        withContext(Dispatchers.Default){  // withcontext is suspend function

            coroutineScope {

            }
        }
    }

}

private suspend fun y(){
    delay(5)

    withContext(Dispatchers.Default){

    }

    coroutineScope {

    }

//    async{                // async coroutine scope builder
//
//    }
//                                    these are only possible inside coroutine scope
//    launch{               // launch coroutine scope builder
//
//    }
}