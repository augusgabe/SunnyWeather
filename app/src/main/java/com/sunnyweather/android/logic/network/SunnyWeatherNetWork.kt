package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetWork {
    private val weatherService = ServiceCreator.create<WeatherService>()
    suspend fun getDailyWeather(lng:String,lat:String)=
        weatherService.getDailyWeather(lng, lat).await()

    suspend fun getRealtimeWeather(lng:String,lat: String)=
        weatherService.getRealtimeWeather(lng, lat).await()

    private val placeService = ServiceCreator.create<PlaceService>()

//    suspend 关键字可把任意函数声明为挂起函数，但无法给其提供协程作用域，调用await()返回协程执行结果，否则返回为Job对象
    suspend fun searchPlaces(query:String) =
        placeService.searchPlaces(query).await()

    private suspend fun <T> Call <T>.await():T{
/*   suspendCoroutine函数必须在协程或挂起函数中才能调用，接收lambda表达式参数，主要作用为将当前协程立即挂起
然后在一个普通线程中执行lambda表达式中的代码，lambda表达式上会上传一个continuation参数，调用它的resume()
或resumeWithException()可以让协程恢复执行    */
        return suspendCoroutine {
                continuation->
            enqueue(object :Callback<T>{
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if(body!=null){
                        continuation.resume(body)
                    }else{
                        continuation.resumeWithException(
                            RuntimeException("response body is null"))
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })

        }
    }
}