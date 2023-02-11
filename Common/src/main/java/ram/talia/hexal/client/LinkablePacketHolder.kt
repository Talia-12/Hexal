package ram.talia.hexal.client

import ram.talia.hexal.common.network.MsgSetRenderLinksAck


/**
 * When packets are sent to the client to update the state of client rendered links, if the think that is being linked to does not yet exist,
 * a record for the packet is added here to retry in 1-20 ticks.
 */
object LinkablePacketHolder {
    private val toRetry: MutableList<MsgSetRenderLinksAck> = mutableListOf() // no need to save state since the server will resend all relevant packets on login.
    private var tick = 0

    fun schedule(packet: MsgSetRenderLinksAck) = toRetry.add(packet)

    /**
     * Called every tick, on every 20th tick will call [retry].
     */
    @JvmStatic
    fun maybeRetry() {
        tick += 1

        if (tick % 20 == 0)
            retry()
    }

    @JvmStatic
    private fun retry() {
        // done so that we can clear toRetry without clearing any new packets that may have been added by the .handle calls.
        val retrying = mutableListOf<MsgSetRenderLinksAck>()
        retrying.addAll(toRetry)
        toRetry.clear()

        for (packet in retrying) {
            MsgSetRenderLinksAck.handle(packet)
        }
    }
}