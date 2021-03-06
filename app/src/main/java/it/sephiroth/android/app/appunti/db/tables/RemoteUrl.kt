package it.sephiroth.android.app.appunti.db.tables

import com.dbflow5.annotation.*
import com.dbflow5.reactivestreams.structure.BaseRXModel
import it.sephiroth.android.app.appunti.db.AppDatabase


@Table(database = AppDatabase::class)
open class RemoteUrl() : BaseRXModel() {

    constructor(other: RemoteUrl) : this() {
        remoteUrlID = other.remoteUrlID
        remoteUrlEntryID = other.remoteUrlEntryID
        remoteUrlTitle = other.remoteUrlTitle
        remoteUrlDescription = other.remoteUrlDescription
        remoteThumbnailUrl = other.remoteThumbnailUrl
        remoteUrlOriginalUri = other.remoteUrlOriginalUri
        remoteUrlVisible = other.remoteUrlVisible
        remoteParsedString = other.remoteParsedString
    }

    @PrimaryKey(autoincrement = true)
    var remoteUrlID: Long = 0

    @Column(defaultValue = "true")
    var remoteUrlVisible: Boolean = true

    @ForeignKey(tableClass = Entry::class, onDelete = ForeignKeyAction.CASCADE)
    var remoteUrlEntryID: Long? = 0

    var remoteUrlTitle: String? = null

    var remoteUrlDescription: String? = null

    /** relative path */
    var remoteThumbnailUrl: String? = null

    /** original uri */
    var remoteUrlOriginalUri: String? = null

    /** parsed string from text */
    var remoteParsedString: String? = null

    @Suppress("unused")
    fun isRemoteUrlVisible() = remoteUrlVisible

    override fun toString(): String {
        return "RemoteUrl(remoteUrlID=$remoteUrlID, remoteUrlVisible=$remoteUrlVisible, remoteUrlEntryID=$remoteUrlEntryID, remoteUrlTitle=$remoteUrlTitle, remoteUrlDescription=$remoteUrlDescription, remoteThumbnailUrl=$remoteThumbnailUrl, remoteUrlOriginalUri=$remoteUrlOriginalUri, remoteParsedString=$remoteParsedString)"
    }
}