package ru.tgfd.core.model

data class Channel(
    val id: Long,
    val title: String,
    val lowQualityAvatar: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Channel

        if (id != other.id) return false
        if (title != other.title) return false
        if (lowQualityAvatar != null) {
            if (other.lowQualityAvatar == null) return false
            if (!lowQualityAvatar.contentEquals(other.lowQualityAvatar)) return false
        } else if (other.lowQualityAvatar != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (lowQualityAvatar?.contentHashCode() ?: 0)
        return result
    }
}
