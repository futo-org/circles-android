package org.futo.circles.auth.bsspeke

object BSSpekeClientProvider {

    private var clientInstance: BSSpekeClient? = null

    fun getClientOrThrow() =
        clientInstance ?: throw IllegalArgumentException("BsSpeke client is not initialized")

    fun initClient(userPart: String, domain: String, password: String) {
        clientInstance = BSSpekeClient("@$userPart:$domain", domain, password)
    }

    fun clear() {
        clientInstance = null
    }

}