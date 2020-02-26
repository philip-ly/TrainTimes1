package com.anfantanion.traintimes1.repositories

import android.content.Context
import android.util.Log
import com.anfantanion.traintimes1.models.Journey
import com.anfantanion.traintimes1.models.parcelizable.JourneyStub
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object JourneyRepo {

    private var activeJourneyUUID : UUID? = null
    var activeJourney: Journey? = null
    set(value) {
        activeJourneyUUID = value?.uuid
        field = value
    }
    get() {
        return journeyLookup[activeJourneyUUID]
    }

    var orderedJourneys = ArrayList<Journey>()
    var journeyLookup = HashMap<UUID,Journey>()
    private const val journeyFIleName = "journeys"


    fun addJourney(journey: Journey){
        if (journey.uuid in journeyLookup){
            orderedJourneys[orderedJourneys.indexOf(journey)] = journey
            journeyLookup[journey.uuid] = journey
        }
        else {
            orderedJourneys.add(journey)
            journeyLookup[journey.uuid] = journey
        }
    }

    fun swapOrder(i : Int, j: Int){
        orderedJourneys[j] = orderedJourneys[i].also {orderedJourneys[i] = orderedJourneys[j]}
    }

    fun getJourney(journeyStub: JourneyStub?): Journey?{
        journeyStub?.let { return journeyLookup[journeyStub.uuid] }
        return null
    }

    fun removeJourney(journey: Journey){
        orderedJourneys.remove(journey)
        journeyLookup.remove(journey.uuid)
    }

    fun getSavedJourneys(): List<Journey>{
        return orderedJourneys
    }

    fun save(context: Context){
        context.openFileOutput(journeyFIleName,Context.MODE_PRIVATE).use{
            ObjectOutputStream(it).use{ it2 ->
                it2.writeObject(orderedJourneys)
                it2.writeObject(activeJourneyUUID)
            }
        }
    }

    fun load(context: Context){
        try {
            context.openFileInput(journeyFIleName).use { fis ->
                ObjectInputStream(fis).use { it2 ->
                    orderedJourneys = it2.readObject() as? ArrayList<Journey> ?: ArrayList()
                    activeJourneyUUID = it2.readObject() as UUID?
                }
            }
        } catch (e: Exception){
            Log.d("SEARCHMANAGER","History File not found ${e.localizedMessage}")
            orderedJourneys = ArrayList()
        }
        for (j in orderedJourneys) journeyLookup[j.uuid] = j
    }



}