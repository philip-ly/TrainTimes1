package com.anfantanion.traintimes1.repositories

import android.content.Context
import android.util.Log
import com.anfantanion.traintimes1.models.Journey
import com.anfantanion.traintimes1.models.parcelizable.JourneyStub
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.collections.HashMap

object JourneyRepo {

    var journeys = HashMap<UUID,Journey>()
    private const val journeyFIleName = "journeys"


    fun addJourney(journey: Journey){
        journeys[journey.uuid] = journey
    }

    fun updateJourney(journey: Journey){
        journeys[journey.uuid] = journey
    }

    fun getJourney(journeyStub: JourneyStub?): Journey?{
        journeyStub?.let { return journeys[journeyStub.uuid] }
        return null
    }

    fun getSavedJourneys(): List<Journey>{
        return journeys.values.toList()
    }

    fun save(context: Context){
        context.openFileOutput(journeyFIleName,Context.MODE_PRIVATE).use{
            ObjectOutputStream(it).use{ it2 ->
                it2.writeObject(journeys)
            }
        }
    }

    fun load(context: Context){
        try {
            context.openFileInput(journeyFIleName).use { fis ->
                ObjectInputStream(fis).use { it2 ->
                    journeys = it2.readObject() as? HashMap<UUID,Journey> ?: HashMap<UUID,Journey>()
                }
            }
        } catch (e: Exception){
            Log.d("SEARCHMANAGER","History File not found ${e.localizedMessage}")
            journeys = HashMap<UUID,Journey>()
        }
    }



}