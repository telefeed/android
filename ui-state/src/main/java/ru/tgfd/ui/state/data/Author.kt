package ru.tgfd.ui.state.data

data class Author(
    val name: String,
    val avatarUrl: String,
    val lowQualityAvatar: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Author

        if (name != other.name) return false
        if (avatarUrl != other.avatarUrl) return false
        if (lowQualityAvatar != null) {
            if (other.lowQualityAvatar == null) return false
            if (!lowQualityAvatar.contentEquals(other.lowQualityAvatar)) return false
        } else if (other.lowQualityAvatar != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + avatarUrl.hashCode()
        result = 31 * result + (lowQualityAvatar?.contentHashCode() ?: 0)
        return result
    }
}
