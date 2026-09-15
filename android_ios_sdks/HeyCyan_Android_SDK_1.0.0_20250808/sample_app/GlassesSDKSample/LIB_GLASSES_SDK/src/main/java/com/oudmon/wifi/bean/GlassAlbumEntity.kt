package com.oudmon.wifi.bean

class GlassAlbumEntity constructor(
    var fileName: String,
    var mac: String,
    var filePath: String,
    var videoFirstFrame: String,
    var fileType: Int,
    var videoLength :Int,
    var fileDate: String,
    var timestamp: Long,
    var horizontalCalibration:Int,
    var userLike:Int,
    var eisInProgress: Boolean = false,
    var editSelect: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GlassAlbumEntity

        if (fileName != other.fileName) return false
        if (mac != other.mac) return false
        if (filePath != other.filePath) return false
        if (videoFirstFrame != other.videoFirstFrame) return false
        if (fileType != other.fileType) return false
        if (videoLength != other.videoLength) return false
        if (fileDate != other.fileDate) return false
        if (timestamp != other.timestamp) return false
        if (horizontalCalibration != other.horizontalCalibration) return false
        if (userLike != other.userLike) return false
        if (eisInProgress != other.eisInProgress) return false
        if (editSelect != other.editSelect) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mac.hashCode()
        result = 31 * result + filePath.hashCode()
        result = 31 * result + videoFirstFrame.hashCode()
        result = 31 * result + fileType
        result = 31 * result + videoLength
        result = 31 * result + fileDate.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + horizontalCalibration
        result = 31 * result + userLike
        result = 31 * result + eisInProgress.hashCode()
        result = 31 * result + editSelect.hashCode()
        return result
    }
}






