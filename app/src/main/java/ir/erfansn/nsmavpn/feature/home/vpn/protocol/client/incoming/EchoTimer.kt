package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.incoming

class EchoTimer(private val interval: Long, private val echoFunction: suspend () -> Unit) {
    private var lastTicked = 0L
    private var deadline = 0L

    private var isEchoWaited = false

    private val isOutOfTime: Boolean
        get() = System.currentTimeMillis() - lastTicked > interval

    private val isDead: Boolean
        get() = System.currentTimeMillis() > deadline

    suspend fun checkAlive(): Boolean {
        if (isOutOfTime) {
            if (isEchoWaited) {
                if (isDead) {
                    return false
                }
            } else {
                echoFunction()
                isEchoWaited = true
                deadline = System.currentTimeMillis() + interval
            }
        }

        return true
    }

    fun tick() {
        lastTicked = System.currentTimeMillis()
        isEchoWaited = false
    }
}
