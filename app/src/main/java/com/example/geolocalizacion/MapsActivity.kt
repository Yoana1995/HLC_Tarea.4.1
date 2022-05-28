package com.example.geolocalizacion

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.geolocalizacion.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocation: FusedLocationProviderClient
    private var ultimoMarcador : Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if(ActivityCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            if (location != null){
                val ubicacion = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 12f))
            }
        }

        val lepe = LatLng(37.3156800, -7.1681700)
        mMap.addMarker(MarkerOptions().position(lepe).title("Marca en Lepe"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lepe))

        mMap.setOnMapClickListener {
            val markerOptions = MarkerOptions().position(it)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN)
            )
            val nombreUbicacion = obtenerDireccion(it)
            markerOptions.title(nombreUbicacion)

            if (ultimoMarcador != null){
                ultimoMarcador!!.remove()
            }
            ultimoMarcador = mMap.addMarker(markerOptions)
            mMap.animateCamera((CameraUpdateFactory.newLatLng(it)))
        }
    }
    fun obtenerDireccion(latLng: LatLng):String{
        val geocoder = Geocoder(this)
        val direcciones : List<Address>?
        val primeraDireccion : Address
        var textoDireccion = ""

        try {
            direcciones = geocoder.getFromLocation(
                latLng.latitude, latLng.longitude, 1)
            if(direcciones != null && direcciones.isNotEmpty()){
                primeraDireccion = direcciones[0]

                if (primeraDireccion.maxAddressLineIndex > 0) {
                    for (i in 0..primeraDireccion.maxAddressLineIndex) {
                        textoDireccion += primeraDireccion.getAddressLine(i) + "\n"
                    }
                }else{
                    textoDireccion += primeraDireccion.thoroughfare + ", "+
                            primeraDireccion.subThoroughfare+"\n"
                }
            }
        }catch (e : Exception){
            textoDireccion="Dirección no encontrada"
        }

        return textoDireccion
    }
}