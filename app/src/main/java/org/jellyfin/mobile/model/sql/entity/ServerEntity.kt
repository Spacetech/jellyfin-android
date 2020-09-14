package org.jellyfin.mobile.model.sql.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.jellyfin.mobile.model.sql.entity.ServerEntity.Key.HOSTNAME
import org.jellyfin.mobile.model.sql.entity.ServerEntity.Key.TABLE_NAME

@Entity(tableName = TABLE_NAME, primaryKeys = [HOSTNAME])
data class ServerEntity(
    @ColumnInfo(name = HOSTNAME) val hostname: String,
    @ColumnInfo(name = USER_ID) val userId: String,
    @ColumnInfo(name = ACCESS_TOKEN) val accessToken: String?,
    @ColumnInfo(name = LAST_ACTIVE_TIME) val lastActiveTime: Long,
    @ColumnInfo(name = LAN_HOSTNAME) val lanHostname: String? = null,
) {
    companion object Key {
        const val TABLE_NAME = "Server"
        const val HOSTNAME = "hostname"
        const val USER_ID = "user_id"
        const val ACCESS_TOKEN = "access_token"
        const val LAST_ACTIVE_TIME = "last_active_time"
        const val LAN_HOSTNAME = "lan_hostname"
    }
}
