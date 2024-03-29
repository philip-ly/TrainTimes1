package com.anfantanion.traintimes1.ui.serviceDetails

import android.app.Service
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.anfantanion.traintimes1.MainActivity
import com.anfantanion.traintimes1.R
import com.anfantanion.traintimes1.models.parcelizable.ServiceStub
import com.anfantanion.traintimes1.models.parcelizable.StationStub
import kotlinx.android.synthetic.main.fragment_service_details.*
import kotlinx.android.synthetic.main.fragment_service_details.serviceDetailsTimingInfoPlanned
import kotlinx.android.synthetic.main.fragment_service_details.serviceDetailsTimingInfoRealTime
import kotlinx.android.synthetic.main.fragment_service_details_listitem.*

/**
 * 
 */
class ServiceDetailsFragment : Fragment(),
    ServiceDetailsRecyclerAdapter.ViewHolder.ViewHolderListener {

    private val TAG = "ServiceDetails"

    val args: ServiceDetailsFragmentArgs by navArgs()

    var focusedStations : Array<StationStub>? = null

    lateinit var serviceStub : ServiceStub
    lateinit var serviceDetailsViewModel: ServiceDetailsViewModel
    lateinit var serviceDetailsRecyclerAdapter: ServiceDetailsRecyclerAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        serviceStub = args.ActiveService
        focusedStations = args.FocusedStation

        Log.d(TAG,serviceStub.serviceUid)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceDetailsViewModel = ViewModelProvider(this).get(ServiceDetailsViewModel::class.java)
        serviceDetailsViewModel.service = serviceStub

        serviceDetailsRecyclerAdapter = ServiceDetailsRecyclerAdapter(this)

        val serviceDetailsRecylerLayout = LinearLayoutManager(context)
        serviceDetailsRecylcerView.layoutManager = serviceDetailsRecylerLayout
        serviceDetailsRecylcerView.adapter = serviceDetailsRecyclerAdapter

        serviceDetailsViewModel.serviceResponse.observe(viewLifecycleOwner, Observer{
            serviceDetailsRecyclerAdapter.locations=it.locations ?: emptyList()
            serviceDetailsRecyclerAdapter.serviceResponse = it
            serviceDetailsRecyclerAdapter.notifyDataSetChanged()
            setTitle()

            focusedStations?.let {focusedStationArray ->
                if (focusedStationArray.isNotEmpty()){
                    serviceDetailsViewModel.getPositionOfStation(focusedStationArray[0])?.let{firstFocused ->
                        serviceDetailsRecylerLayout.scrollToPosition(firstFocused)
                    }
                }
                serviceDetailsRecyclerAdapter.focused = focusedStationArray.asList()
            }
        })

        serviceDetailsViewModel.isLoading.observe(viewLifecycleOwner,Observer{
                b -> when(b){
            true -> serviceDetailsProgressBar.visibility = View.VISIBLE
            false -> serviceDetailsProgressBar.visibility = View.GONE
        }
        })

        serviceDetailsViewModel.isError.observe(viewLifecycleOwner,Observer{
                b -> when(b){
            true -> Toast.makeText(context,"Error "+serviceDetailsViewModel.lastError.toString(), Toast.LENGTH_LONG).show()
        }
        })

        serviceDetailsTimingInfoPlanned.setOnLongClickListener {
            Toast.makeText(context,R.string.serviceDetailsLegendRTHint,Toast.LENGTH_LONG).show()
            return@setOnLongClickListener false
        }

        serviceDetailsTimingInfoRealTime.setOnLongClickListener {
            Toast.makeText(context,R.string.serviceDetailsLegendRTHint,Toast.LENGTH_LONG).show()
            return@setOnLongClickListener false
        }



        serviceDetailsViewModel.getServiceDetails()
        setColumnTitle(serviceDetailsRecyclerAdapter.timeDisplayType.current)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_servicedetails, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_serviceDetails_refresh -> serviceDetailsViewModel.getServiceDetails()
            R.id.action_serviceDetails_timeToggle -> {
                var x = serviceDetailsRecyclerAdapter.timeDisplayType.next()
                serviceDetailsRecyclerAdapter.notifyDataSetChanged()
                when (x){
                    ServiceDetailsRecyclerAdapter.TimeView.Types.REALTIME ->
                        Toast.makeText(context,R.string.action_serviceDetails_timeToggle_Realtime,Toast.LENGTH_SHORT).show()
                    ServiceDetailsRecyclerAdapter.TimeView.Types.BOOKEDTIME ->
                        Toast.makeText(context,R.string.action_serviceDetails_timeToggle_Booked,Toast.LENGTH_SHORT).show()
                    ServiceDetailsRecyclerAdapter.TimeView.Types.NONE ->
                        Toast.makeText(context,R.string.action_serviceDetails_timeToggle_None,Toast.LENGTH_SHORT).show()
                }
                setColumnTitle(x)

            }
            R.id.action_serviceDetails_onMap ->  findNavController().navigate(
                ServiceDetailsFragmentDirections.actionServiceDetailsToMapsFragment(arrayOf(serviceDetailsViewModel.service))
            )
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setColumnTitle(type : ServiceDetailsRecyclerAdapter.TimeView.Types){
        when (type){
            ServiceDetailsRecyclerAdapter.TimeView.Types.REALTIME -> {
                serviceDetailsTimingInfoPlanned.visibility = View.GONE
                serviceDetailsTimingInfoRealTime.visibility = View.VISIBLE
            }
            ServiceDetailsRecyclerAdapter.TimeView.Types.BOOKEDTIME -> {
                serviceDetailsTimingInfoPlanned.visibility = View.VISIBLE
                serviceDetailsTimingInfoRealTime.visibility = View.GONE
            }
            ServiceDetailsRecyclerAdapter.TimeView.Types.NONE -> {
                serviceDetailsTimingInfoPlanned.visibility = View.GONE
                serviceDetailsTimingInfoRealTime.visibility = View.GONE
            }
        }
    }

    private fun setTitle() {
        (activity as MainActivity).supportActionBar?.title = serviceDetailsViewModel.serviceResponse.value?.getName() ?: getString(R.string.fragment_serviceDetails_title)
    }

    override fun onMainClick(position: Int) {
        val x = serviceDetailsViewModel.serviceResponse.value?.locations?.get(position)
        if (x != null) {
            val stationStub = StationStub(x.crs)
            findNavController().navigate(
                ServiceDetailsFragmentDirections.actionServiceDetailsToStationDetails(stationStub)
            )
        }
        else
            Log.d(TAG,"Error finding service")
    }

    override fun onAdditionalInfoButtonClick(position: Int) {
        val focusedLocation = serviceDetailsViewModel.serviceResponse.value?.locations?.get(position)
        val associatedService = focusedLocation?.associations?.get(0)?.toServiceStub()
        if (associatedService != null) {
            findNavController().navigate(
                ServiceDetailsFragmentDirections.actionServiceDetailsSelf(associatedService,arrayOf(focusedLocation.toStationStub()))
            )
        }
        else
            Log.d(TAG,"Error finding service")
    }

    override fun showAssociations(): Boolean {
        return true
    }


}
