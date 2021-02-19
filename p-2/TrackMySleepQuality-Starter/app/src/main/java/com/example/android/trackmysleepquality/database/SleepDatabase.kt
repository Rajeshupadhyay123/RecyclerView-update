package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    abstract val sleepDatabaseDao: SleepDatabaseDao

    companion object {
        /**
         * INSTANCE:-
         * The INSTANCE variable will keep a reference to the database, when one has been created.
         * This helps you avoid repeatedly opening connections to the database,
         * which is computationally expensive.
         *
         * Volatile:
         * The value of Volatile variable is never chached, and all the read or write are done
         * by main memory
         *
         * This helps make sure that the value of INSTANCE is always up-to-date. It means that
         * changes made by one thread to INSTANCE are visible to all other threads
         * immediately, you don't get a situation where, say, two threads each update
         * the same entity in a cache, which would create a problem.
         *
         */
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getInstance(context: Context): SleepDatabase {
            /**
             * Multiple threads can potentially ask for a database instance at the same
             * time, resulting in two databases instead of one.
             * Wrapping the code to get the database into synchronized means that only
             * one thread of execution at a time can enter this block of code, which
             * makes sure the database only gets initialized once.
             */
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE=instance
                }
                /**
                 * If you don’t want to provide migrations and you specifically want your database
                 * to be cleared when you upgrade the version, call fallbackToDestructiveMigration
                 * in the database builder
                 * for more:https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                 */
                return instance
            }
        }
    }
}
