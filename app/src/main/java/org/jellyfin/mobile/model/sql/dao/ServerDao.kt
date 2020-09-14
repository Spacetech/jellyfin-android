package org.jellyfin.mobile.model.sql.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.jellyfin.mobile.model.sql.entity.ServerEntity
import org.jellyfin.mobile.model.sql.entity.ServerEntity.Key.ACCESS_TOKEN
import org.jellyfin.mobile.model.sql.entity.ServerEntity.Key.HOSTNAME
import org.jellyfin.mobile.model.sql.entity.ServerEntity.Key.LAST_ACTIVE_TIME
import org.jellyfin.mobile.model.sql.entity.ServerEntity.Key.TABLE_NAME

@Dao
interface ServerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ServerEntity)

    fun insert(hostname: String, userId: String, accessToken: String) {
        insert(ServerEntity(hostname, userId, accessToken, System.currentTimeMillis()))
    }

    @Query("SELECT * FROM $TABLE_NAME WHERE $ACCESS_TOKEN IS NOT NULL ORDER BY $LAST_ACTIVE_TIME DESC LIMIT 1")
    fun queryLastUsedSever(): ServerEntity?

    @Query("UPDATE $TABLE_NAME SET $ACCESS_TOKEN = NULL WHERE $HOSTNAME = :hostname")
    fun logout(hostname: String)
}
