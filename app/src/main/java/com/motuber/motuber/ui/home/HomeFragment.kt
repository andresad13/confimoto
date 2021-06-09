package com.motuber.motuber.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.motuber.motuber.R

class HomeFragment : Fragment(), OnMapReadyCallback {

     lateinit var homeViewModel: HomeViewModel
     var mFusedLocationProviderClient: FusedLocationProviderClient? = null
     val INTERVAL: Long = 2000
     val FASTEST_INTERVAL: Long = 1000
     lateinit var map: GoogleMap
      var flagMarker  = false
    var flagVerify = 0
    private var mDatabase: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth

    lateinit var locOrigen : Location
    lateinit var locDestino : Location
    private var latitudDes: Double = 0.0
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    lateinit var  placeNamedestino : String
    private var longitudDes: Double = 0.0
    lateinit var context: Fragment
    private lateinit var buttFindLocation: Button
    private lateinit var buttFindDriver: Button


    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)


        mDatabase = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()



        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            context = this
            mLocationRequest = LocationRequest()


            // Initialize the SDK
            Places.initialize( context.requireContext(), "AIzaSyAJWMVouondqYt13N0NlRD4tLJhSlBpBZk")


            // Create a new PlacesClient instance
            val placesClient = Places.createClient(context.requireContext())

            buttFindLocation = root.findViewById(R.id.BuscarUbicacion)



            buttFindDriver = root.findViewById(R.id.BuscarConductor)


            buttFindDriver.setOnClickListener {

                root.findViewById<LinearLayout>(R.id.busquedaview).visibility = View.VISIBLE
                root.findViewById<LinearLayout>(R.id.origendestinoview).visibility = View.GONE

                mDatabase?.child("rides")?.child(auth.currentUser.uid)?.child("user")?.setValue(auth.currentUser?.email.toString())
                mDatabase?.child("rides")?.child(auth.currentUser.uid)?.child("status")?.setValue("created")
                mDatabase?.child("rides")?.child(auth.currentUser.uid)?.child("origenlat")?.setValue(locOrigen.latitude)
                mDatabase?.child("rides")?.child(auth.currentUser.uid)?.child("origenlon")?.setValue(locOrigen.longitude)
                mDatabase?.child("rides")?.child(auth.currentUser.uid)?.child("destinolat")?.setValue(locDestino.latitude)
                mDatabase?.child("rides")?.child(auth.currentUser.uid)?.child("destinolon")?.setValue(locDestino.longitude)
                mDatabase?.child("rides")?.child(auth.currentUser.uid)?.child("destinoname")?.setValue(placeNamedestino)

            }

            var firebaseDatabase: FirebaseDatabase
            firebaseDatabase = FirebaseDatabase.getInstance();

            val reflatitude = firebaseDatabase.getReference("rides/"+auth.currentUser.uid)
            reflatitude.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {



                    println("servicio: "+dataSnapshot.toString())

                    if (dataSnapshot.child("status").value.toString().equals("reserved")
                    ) {
                        root.findViewById<TextView>(R.id.conductorName).setText(dataSnapshot.child("driver").value.toString())
                        root.findViewById<LinearLayout>(R.id.esperaview).visibility = View.VISIBLE

                        root.findViewById<LinearLayout>(R.id.busquedaview).visibility = View.GONE
                        root.findViewById<LinearLayout>(R.id.origendestinoview).visibility = View.GONE
                    }
                    if (dataSnapshot.child("status").value.toString().equals("initialized")
                    ) {
                        root.findViewById<Button>(R.id.espera_conductor).isEnabled = false
                        root.findViewById<Button>(R.id.espera_conductor).setText("conductor ha llegado!")
                    }
                    if (dataSnapshot.child("status").value.toString().equals("inprogress")
                    ) {
                        root.findViewById<Button>(R.id.espera_conductor).isEnabled = false
                        root.findViewById<Button>(R.id.espera_conductor).setText("viaje en progreso!")
                    }
                    if (dataSnapshot.child("status").value.toString().equals("finished")
                    ) {
                        root.findViewById<Button>(R.id.espera_conductor).isEnabled = true
                        root.findViewById<Button>(R.id.espera_conductor).setText("Calificar")

                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(null, "Failed to read value.", error.toException())
                }
            })



            buttFindLocation.setOnClickListener {
                if (flagVerify == 2){
                    println("a continuar")

                    root.findViewById<LinearLayout>(R.id.bar_bottomhomeRide).visibility = View.VISIBLE
                    root.findViewById<TextView>(R.id.textDestino).setText(placeNamedestino)



                }
                setDestino(latitudDes, longitudDes)



            }

            autocompleteFragment = context.childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                        as AutocompleteSupportFragment


            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    println( "Place: ${place.name}, ${place.id}. ${place.latLng} ")

                    latitudDes = place.latLng!!.latitude
                    longitudDes = place.latLng!!.longitude
                    placeNamedestino = place.name.toString()


                }

                override fun onError(p0: com.google.android.gms.common.api.Status) {
                    Log.i("places", "An error occurred: $p0")
                }


            })


            createMapFragment()

            if (checkPermissionForLocation(context.requireContext())) {
                startLocationUpdates()

            }
            else{
                checkPermissionForLocation(context.requireContext())
            }


        })
        return root
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        initCameraIdleListener()

    }

    fun initCameraIdleListener() {
        var latitude = map.cameraPosition.target.latitude
        var longitude = map.cameraPosition.target.longitude
        var myLatLng = LatLng(latitude, longitude)

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18f))

    }
        private fun createMapFragment() {
        val mapFragment = childFragmentManager?.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined

        mLastLocation = location

        locOrigen = mLastLocation
        val favoritePlace = LatLng(mLastLocation.latitude,mLastLocation.longitude)
        // You can now create a LatLng Object for use with maps

        if (flagMarker == false) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(favoritePlace, 18f), 200,  null)
                flagMarker = true
        }
    }
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // do work here
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun setDestino(lat:Double, lon: Double){

        val cameraPosition = CameraPosition.Builder().target(LatLng(lat, lon)).zoom(17.0.toFloat()).build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        locDestino  =  Location("")
        locDestino.latitude = lat
        locDestino.longitude = lon

        map?.moveCamera(cameraUpdate)
        buttFindLocation.setText("Ir aquí...")
        flagVerify += 1
    }

    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.setInterval(INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(context.requireContext())
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context.requireContext())
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(context.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context.requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }


    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // Show the permission request

                false
            }
        } else {
            true
        }
    }
    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(context.requireContext())
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()


    }


}