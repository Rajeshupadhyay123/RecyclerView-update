/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    lateinit var dataSource: SleepDatabaseDao

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)

        // Get a reference to the ViewModel associated with this fragment.
        val sleepTrackViewModel = ViewModelProvider(this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        //This is for providing basic data binding in the fragment that interact with viewModel directly
        binding.lifecycleOwner = viewLifecycleOwner
        binding.sleepTrackerViewModel = sleepTrackViewModel

        sleepTrackViewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                val action = SleepTrackerFragmentDirections.ActionSleepTrackerFragmentToSleepQualityFragment(night.nightId)
                this.findNavController().navigate(action)
            }
        })

        val adapter = SleepNightAdapter(SleepNightListener { nightId->
            Toast.makeText(context,"${nightId}",Toast.LENGTH_SHORT).show()
            sleepTrackViewModel.onSleepNightClicked(nightId)
        })
        binding.sleepList.adapter = adapter

        sleepTrackViewModel.nights.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        /**
         * Here when the navigateToSleepDetail livedata value has changed then we call the obsrver
         * and navigate to the detail fragment
         *
         * Note:
         * Important point to be care:
         *      Here the live data value has the null by-default and after performing the operation
         *      it got changes. But the point is it create an null pointer exception that we are
         *      calling this live data value as an item:SleepNight in the BindingAdapter so it is not
         *      possible to call the value on the null reference. so we have to check this null value
         *      in the binding Adapter. we can simply use the item.let{ .. } for handing
         */
        sleepTrackViewModel.navigateToSleepDetail.observe(viewLifecycleOwner, Observer {night->
            night?.let {
                this.findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(night)
                )
                sleepTrackViewModel.onSleepDetailNavigated()
            }
        })

        //grid manager
        val manager = GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)
        binding.sleepList.layoutManager = manager

        return binding.root
    }
}
