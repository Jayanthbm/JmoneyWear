package com.jayanth.jmoney.wear.data.sync

/**
 * Fetches every page before returning a snapshot to its caller.
 *
 * If any page fails, the exception escapes and no partial list is returned.
 */
internal suspend fun <T> fetchCompleteRemoteSnapshot(
    pageSize: Int = 1000,
    fetchPage: suspend (offset: Int, pageSize: Int) -> List<T>
): List<T> {
    require(pageSize > 0) { "pageSize must be positive" }

    val snapshot = mutableListOf<T>()
    var offset = 0
    while (true) {
        val page = fetchPage(offset, pageSize)
        if (page.isEmpty()) return snapshot
        snapshot += page
        if (page.size < pageSize) return snapshot
        offset += page.size
    }
}

/**
 * Prevents a transient empty remote response from erasing a populated local
 * snapshot. A genuinely empty remote + local dataset remains valid.
 */
internal fun requireSafeRemoteSnapshot(
    remoteCount: Int,
    localCount: Int,
    entityName: String
) {
    require(remoteCount >= 0 && localCount >= 0) { "Snapshot counts cannot be negative" }
    if (remoteCount == 0 && localCount > 0) {
        throw IllegalStateException(
            "Remote snapshot for $entityName is empty while local records exist; refusing destructive replacement"
        )
    }
}
