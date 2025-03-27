package com.example.trackify.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackify.data.RunEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.trackify.repository.MainRepository
import com.example.trackify.util.SortType
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    fun getAllRunsSortedByDate() = mainRepository.getAllRunsSortedByDate()
    fun getAllRunsSortedByDistance() = mainRepository.getAllRunsSortedByDistance()
    fun getAllRunsSortedByTimeInMillis() = mainRepository.getAllRunsSortedByTimeInMillis()
    fun getAllRunsSortedByAvgSpeed() = mainRepository.getAllRunsSortedByAvgSpeed()
    fun getAllRunsSortedByCaloriesBurned() = mainRepository.getAllRunsSortedByCaloriesBurned()


    val runs = MediatorLiveData<List<RunEntity>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(getAllRunsSortedByDate()) { result ->
            if (sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(getAllRunsSortedByTimeInMillis()) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(getAllRunsSortedByAvgSpeed()) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(getAllRunsSortedByDistance()) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(getAllRunsSortedByCaloriesBurned()) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
    }


    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> getAllRunsSortedByDate().value?.let { runs.value = it }
        SortType.RUNNING_TIME -> getAllRunsSortedByTimeInMillis().value?.let { runs.value = it }
        SortType.AVG_SPEED -> getAllRunsSortedByAvgSpeed().value?.let { runs.value = it }
        SortType.DISTANCE -> getAllRunsSortedByDistance().value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> getAllRunsSortedByCaloriesBurned().value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: RunEntity) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }


}
