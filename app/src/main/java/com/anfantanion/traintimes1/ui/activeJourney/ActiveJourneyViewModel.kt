package com.anfantanion.traintimes1.ui.activeJourney

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.VolleyError
import com.anfantanion.traintimes1.models.ActiveJourney
import com.anfantanion.traintimes1.models.Journey
import com.anfantanion.traintimes1.models.Station
import com.anfantanion.traintimes1.models.TimeDate
import com.anfantanion.traintimes1.models.journeyPlanners.JourneyPlannerError
import com.anfantanion.traintimes1.models.parcelizable.ServiceStub
import com.anfantanion.traintimes1.models.parcelizable.StationStub
import com.anfantanion.traintimes1.models.stationResponse.Service
import com.anfantanion.traintimes1.models.stationResponse.ServiceResponse
import com.anfantanion.traintimes1.repositories.JourneyRepo
import com.anfantanion.traintimes1.repositories.RTTAPI
import com.anfantanion.traintimes1.repositories.StationRepo

class ActiveJourneyViewModel : ViewModel() {

    var activeJourney = MutableLiveData<ActiveJourney?>(null)

    var serviceResponses = MutableLiveData<List<ServiceResponse>>()

    var loadedPreviouslyPlannedRoute = MutableLiveData<Boolean>(false)

    var isLoading = MutableLiveData<Boolean>(false)
    var isError = MutableLiveData<Boolean>(false)
    var lastError = VolleyError()
    var errorText = MutableLiveData<String>()

    var refreshAge = MutableLiveData<Int?>(null)

    init {
        val temp = JourneyRepo.activeJourney
        if (temp!=null){
            if (temp.date?.getDate() != null && temp.date?.getDate() != TimeDate().getDate()){
                JourneyRepo.activeJourney = null
                errorText.value = "Active Journey has Expired"
            }else {
                activeJourney.value = temp
            }
        }
    }


    fun getServices(
        forceReplan: Boolean = false
    ){
        val journey = activeJourney.value
        if (journey == null) {
            isError.value = true
            return
        }
        isLoading.value = true
        loadedPreviouslyPlannedRoute.value = journey.getPlannedServices({onPlanned(it)},{onError(it)},forceReplan)
    }

    fun getWaypointStations(): List<Station>?{
        if (activeJourney.value == null) return null
        return activeJourney.value!!.waypoints.mapNotNull{sS -> StationRepo.getStation(sS) }
    }


    fun getCurrentServiceNo() : Int?{
        val serviceResponses = serviceResponses.value ?: return null
        return serviceResponses.indexOf(getCurrentService())
    }

    fun getCurrentService() : ServiceResponse {
        val aj = activeJourney.value!!

        for (i in serviceResponses.value!!.indices){
            val x = serviceResponses.value!!.filter{ sr ->
                TimeDate(startTime = sr.getRTStationDeparture(aj.waypoints[i])).calendar.timeInMillis < TimeDate().calendar.timeInMillis &&
                        TimeDate(startTime = sr.getRTStationDeparture(aj.waypoints[i+1])).calendar.timeInMillis > TimeDate().calendar.timeInMillis
            }
            if (x.isNotEmpty()) return x.first()
        }
        return serviceResponses.value!![0]
    }

    private fun onPlanned(serviceStubs : List<ServiceStub>?){
        if (serviceStubs == null){
            isError.value = true
            return
        }
        RTTAPI.requestServices(
            serviceStubs,
            listener = {
                val x = Array<ServiceResponse?>(serviceStubs.size){null}
                if (it.isEmpty()) return@requestServices
                it.forEach {sr ->
                    x[serviceStubs.indexOf(sr.toServiceStub())] = sr
                }
                serviceResponses.value = x.toList().filterNotNull()
                isLoading.value = false
            },
            errorListener = Response.ErrorListener { error ->
                lastError = error
                isError.value = true
                isLoading.value = false
            }

        )

    }

    private fun onError(journeyPlannerError: JourneyPlannerError){
        isError.value = true
        isLoading.value = false
        errorText.value = when (journeyPlannerError.type){
            JourneyPlannerError.ErrorType.VOLLEYERROR -> {journeyPlannerError.volleyError?.localizedMessage}
            JourneyPlannerError.ErrorType.NOSERVICEFOUND -> {journeyPlannerError.reason+journeyPlannerError.errors?.toString()}
            JourneyPlannerError.ErrorType.OTHER -> "Unknown"
        }
        errorText.value = journeyPlannerError.reason

    }

    fun getNextChange() : Change? {
        val x = getCurrentServiceNo() ?: return null
        return getChanges()?.get(x)
    }

    fun getChanges(): List<Change>?{

        val journeyPlan = serviceResponses.value ?: return null
        val activeJourney = activeJourney.value ?: return null
        if (journeyPlan.size <= 1) return null
        if (journeyPlan.size == 1) return null

        val changes = ArrayList<Change>()
        for (i in 0..journeyPlan.size-2){
            val c = Change(
                journeyPlan[i],
                journeyPlan[i+1],
                activeJourney.waypoints[i+1]
                )
            changes.add(c)
        }
        return changes
    }



    class Change(
        val service1 : ServiceResponse,
        val service2 : ServiceResponse,
        val waypoint : StationStub
    ){
        fun arrivalTime() : String?{
            return service1.getRTStationArrival(waypoint)
        }

        fun departureTime() : String?{
            return service2.getRTStationDeparture(waypoint)
        }


    }

}