package services

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface GoogleMapService {

    @GET("Direction")
    fun getDirections(@QueryMap _filter: HashMap<String, String>?):Call<ResponseBody>  //Call<List<GoogleMapDTO>>  Direction
}