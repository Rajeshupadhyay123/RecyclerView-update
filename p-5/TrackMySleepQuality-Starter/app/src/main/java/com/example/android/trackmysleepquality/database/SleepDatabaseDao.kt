
package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SleepDatabaseDao{

    @Insert
    suspend fun insert(night:SleepNight)

    @Update
    suspend fun update(night: SleepNight)

    //Notice the :key. You use the colon notation in the query to reference arguments in the function.
    @Query("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
    suspend fun get(key:Long):SleepNight?

    @Query("DELETE FROM daily_sleep_quality_table")
    suspend fun clear()

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    suspend fun getTonight():SleepNight?

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNight(): LiveData<List<SleepNight>>
}

/**
 * Why uses of 'suspend':-
 * The keyword suspend is Kotlin's way of marking a function, or function type,
 * as being available to coroutines.
 *
 * Here we doesn't need use the suspend keyword in the getAllNight() function bez the
 * Room already uses a background thread for LiveData
 */
