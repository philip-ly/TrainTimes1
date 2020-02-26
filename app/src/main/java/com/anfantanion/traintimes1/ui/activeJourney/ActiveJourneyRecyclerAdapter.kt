package com.anfantanion.traintimes1.ui.activeJourney

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anfantanion.traintimes1.R
import com.anfantanion.traintimes1.models.Journey
import com.anfantanion.traintimes1.models.stationResponse.ServiceResponse
import kotlinx.android.synthetic.main.fragment_active_journey_listitem.view.*

class ActiveJourneyRecyclerAdapter(
    var viewHolderListener: ActiveJourneyViewHolder.ViewHolderListener
) : RecyclerView.Adapter<ActiveJourneyRecyclerAdapter.ActiveJourneyViewHolder>() {

    var services = emptyList<ServiceResponse>()

    class ActiveJourneyViewHolder(itemView: View, private var viewHolderListener: ViewHolderListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnTouchListener{
        val titleLayout = itemView.activeJourneyListItemTitleLayout!!
        val title = itemView.activeJourneyListItemTitle!!

        val change = itemView.activeJourneyListItemChangeAt!!

        val activeJourneyListItemRecycler = itemView.activeJourneyListItemRecycler!!

        val serviceDetailsButton = itemView.activeJourneyListItemButtonServiceDetails !!
        val mapButton = itemView.activeJourneyListItemButtonMap!!


        init {
            itemView.setOnClickListener(this)
            itemView.setOnTouchListener(this)
            serviceDetailsButton.setOnClickListener(this)
            mapButton.setOnClickListener(this)
        }


        override fun onClick(v: View?) {
            when (v){
                serviceDetailsButton -> viewHolderListener.onDetailsButtonClick(adapterPosition)
                mapButton -> viewHolderListener.onMapButtonClick(adapterPosition)
                else -> viewHolderListener.onItemJourneyClick(adapterPosition)
            }

        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event?.action == MotionEvent.ACTION_DOWN) {
                viewHolderListener.dragImageTouchDown(this,adapterPosition)
            }
            return false
        }

        interface ViewHolderListener{
            fun onItemJourneyClick(position: Int)
            fun onDetailsButtonClick(position: Int)
            fun onMapButtonClick(position: Int)
            fun dragImageTouchDown(viewHolder: RecyclerView.ViewHolder, position: Int)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveJourneyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_active_journey_listitem,parent,false)
        return ActiveJourneyViewHolder(view,viewHolderListener)
    }

    override fun getItemCount(): Int {
        return services.size
    }

    override fun onBindViewHolder(holder: ActiveJourneyViewHolder, position: Int) {
        val context = holder.itemView.context
        val service = services[position]

    }

}