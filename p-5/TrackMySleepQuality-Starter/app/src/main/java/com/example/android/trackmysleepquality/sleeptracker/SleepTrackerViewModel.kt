
package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {



    //it is for updating the UI
    private val nights:LiveData<List<SleepNight>> = database.getAllNight()
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    //this tonight variable is used for holding the current night data
    private var tonight = MutableLiveData<SleepNight?>()

    //for navigating to the SleepQualityFragment
    private val _navigateToSleepQuality=MutableLiveData<SleepNight>()
    val navigateToSleepQuality:LiveData<SleepNight>
    get()=_navigateToSleepQuality


    val startButtonVisible=Transformations.map(tonight){
        it==null
    }
    val stopButtonVisible=Transformations.map(tonight){
        it!=null
    }
    val clearButtonVisible=Transformations.map(nights){
        it?.isNotEmpty()
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        viewModelScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }

    fun onStopTracking(){
        viewModelScope.launch {
            val oldNight=tonight.value?: return@launch //notice
            oldNight.endTimeMilli=System.currentTimeMillis()
            update(oldNight)
            _navigateToSleepQuality.value=oldNight
        }
    }

    private suspend fun update(night: SleepNight){
        database.update(night)
    }

    fun onClear(){
        viewModelScope.launch {
            clear()
            tonight.value=null
        }
    }

    suspend fun clear(){
        database.clear()
    }

    fun doneNavigating(){
        _navigateToSleepQuality.value=null
    }
}
/**
 * Note:-
 *1. Launch a coroutine that runs on the main UI thread, because the result from that
 *  coroutine affects what is displayed in the UI. You can access the CoroutineScope of
 *  a ViewModel through the viewModelScope property of the ViewModel, as shown in the
 *  following example:

2. Call a suspend function to do the long-running work, so that you don't block the UI
thread while waiting for the result.

3. The result of the long-running work may affect the UI, but its operation is
independent from the UI. For efficiency, switch to the I/O dispatcher,
(Room generates his code for you). The I/O dispatcher uses a thread pool that's optimized
and set aside for these kinds of operations.

4. Then call the long running function to do the work.

The pattern is look like this:
// Using Room
fun someWorkNeedsToBeDone {
viewModelScope.launch {
suspendDAOFunction()
}
}

suspend fun suspendDAOFunction() {
// No need to specify the Dispatcher, Room uses Dispatchers.IO.
longrunningDatabaseWork()
}
 */
