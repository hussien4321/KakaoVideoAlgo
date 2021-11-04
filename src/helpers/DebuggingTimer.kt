package helpers

abstract class DebuggingTimer{
    private var startDebuggingTime: Long = 0L

    fun startDebuggingTimer() { startDebuggingTime = System.currentTimeMillis() }
    fun stopDebuggingTimer(functionName: String, resetTimer: Boolean = true) {

        println("***** FUNC:${functionName} took ${System.currentTimeMillis()-startDebuggingTime}ms!")
        if(resetTimer) {
            startDebuggingTimer()
        }
    }

}