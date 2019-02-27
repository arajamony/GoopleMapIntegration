package services

import com.gmail.rocka.gooplemapintegration.model.GoogleMapDTO
import com.gmail.rocka.gooplemapintegration.model.GoogleMapModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface GoogleMapService {

    @GET
    fun getDirections(@Url anotherUrl:String):Call<ResponseBody>  //Call<List<GoogleMapDTO>>
}