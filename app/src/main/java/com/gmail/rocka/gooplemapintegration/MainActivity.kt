package com.gmail.rocka.gooplemapintegration

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.gmail.rocka.gooplemapintegration.model.GoogleMapDTO
import com.gmail.rocka.gooplemapintegration.model.GoogleMapModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import services.GoogleMapService
import services.ServiceBuilder



private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity() {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap:GoogleMap

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(permissions)) {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
            else
                MarkLocationOnMap()
        }
        else
            MarkLocationOnMap()
    }


    @SuppressLint("MissingPermission")
    private fun MarkLocationOnMap() {
        mapFragment= supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment

        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            googleMap.isMyLocationEnabled=true

            val location1=LatLng(13.0356745,77.5881522)
            //googleMap.addMarker(MarkerOptions().position(location1).title("My Home"))
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location1,5f))

            val location2=LatLng(10.79,78.70)
           // googleMap.addMarker(MarkerOptions().position(location2).title("Trichy"))

            val location3=LatLng(13.029727,77.5933021)
            //googleMap.addMarker(MarkerOptions().position(location3).title("Madurai"))

            Log.d("GoogleMap", "before URL")
            val uRL = getDirectionURL(location1,location3)
            Log.d("GoogleMap", "URL : $uRL")
            drawDirection(uRL)
        })
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                            permissions[i]
                        )
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            if(allSuccess)
                MarkLocationOnMap()
        }
    }

   private fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val bundle = ai.metaData
        val myApiKey = bundle.getString("com.google.android.geo.Direction_API_KEY")
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=$myApiKey"
    }


    private fun drawDirection(url:String) {
        val googleMapService: GoogleMapService = ServiceBuilder.buildService(GoogleMapService::class.java)
        val requestCall: Call<ResponseBody> = googleMapService.getDirections(url)
        val result = ArrayList<List<LatLng>>()
        requestCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val directionList: ResponseBody? = response.body()!!
                    directionList?.let {
                        Log.d("GoogleMap", " data : $directionList")
                        return@let try {
                            val respObj = Gson().fromJson(directionList.string(), GoogleMapDTO::class.java)
                            if (respObj.status == "OK") {
                                val path = ArrayList<LatLng>()

                                for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {
                                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                                }
                                result.add(path)
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Status:${respObj.status} & Error_Message:${respObj.error_message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (!result.isNullOrEmpty())
                        DrawPolyLine(result)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to retrive the Direction", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

private fun DrawPolyLine(result: List<List<LatLng>>) {
    val lineoption = PolylineOptions()
    if(!result.isNullOrEmpty()) {
        for (i in result.indices) {
            lineoption.addAll(result[i])
            lineoption.width(10f)
            lineoption.color(Color.BLUE)
            lineoption.geodesic(true)
        }

        lineoption.startCap(CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.home_address_24)))
        lineoption.endCap(CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.destination_24)))
        /*googleMap.addMarker(MarkerOptions().position(result.first().first()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_home_24)))
    googleMap.addMarker(MarkerOptions().position(result.last().last()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_home_24)))*/
        googleMap.addPolyline(lineoption)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result[0][0], 16F))
    }
}

    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

}
