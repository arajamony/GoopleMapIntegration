package services

import android.os.Build
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

object ServiceBuilder {

    //Base URL for Connecting the service
    private const val URL = "http://18.191.209.98/webapp1_dev/api/"
    //private const val URL = "http://192.168.1.4/WebApp1/api/"

    //Create Logger
    private val logger: HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    //Create Custom Interceptor for adding Header Globally to all the request

    val headerInterceptor:Interceptor= object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request:Request=chain.request()

            request= request.newBuilder()
                .addHeader("x-device-type",Build.DEVICE)
                .addHeader("Accept-Language",Locale.getDefault().language)
                .build()
            var response:Response=chain.proceed(request)
            return response
        }
    }

    //Create OkHttp Client
    private val okHttp: OkHttpClient.Builder = OkHttpClient.Builder()
        .callTimeout(30,TimeUnit.SECONDS) // to increase the Timeout for fetch response from server
        .addInterceptor(headerInterceptor)
        .addInterceptor(logger)

    //Create Retrofit Client
    private val builder: Retrofit.Builder = Retrofit.Builder().baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttp.build())

    //create Instance for retrofit
    private val retrofit: Retrofit = builder.build()

    fun <T> buildService(serviceType: Class<T>): T {
        return retrofit.create(serviceType)
    }

}