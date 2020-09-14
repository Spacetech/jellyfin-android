package org.jellyfin.mobile.model.sql

import androidx.room.Database
import androidx.room.RoomDatabase
import org.jellyfin.mobile.model.sql.dao.ServerDao
import org.jellyfin.mobile.model.sql.entity.ServerEntity

@Database(entities = [ServerEntity::class], version = 1)
abstract class JellyfinDatabase : RoomDatabase() {
    abstract val serverDao: ServerDao
}
