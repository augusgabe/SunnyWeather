package com.sunnyweather.android.logic

import android.content.Context
import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.Exception
import kotlin.RuntimeException
import kotlin.coroutines.CoroutineContext

//仓库类，用于判断调用方请求的数据应该从本地数据源中获取还是从网络源中获取，并将获取的数据返回调用方，本应用只采用网络源
object Repository {
    //Dispatchers.Default/IO/Main,三种分别为不同的线程参数，三者分别为低并发，较高并发，（不开启子线程，只执行主线程
//    此处线程参数指定为Dispatchers.IO,代表代码块中的所有代码都能运行在子线程中了，Android不允许主线程进行网络请求
    fun searchPlaces(query:String)= fire(Dispatchers.IO) {
//        val result = try {
            val placeResponse = SunnyWeatherNetWork.searchPlaces(query)
            if(placeResponse.status == "ok"){
                val places = placeResponse.places
                //内置函数，返回包装好的数据列表
                Result.success(places)
            }else{
                //包装异常信息
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
//        }catch (e:Exception){
//            Result.failure<List<Place>>(e)
//        }
//        //发射结果
//        emit(result)
    }

    fun refreshWeather(lng:String,lat:String)= fire(Dispatchers.IO){
//        val result=try {
            //coroutineScope函数创建协程作用域
            coroutineScope {
                //async函数可保证当以下两个网络请求都成功响应后，才会进一步执行程序
                val deferredRealtime=async {
                    SunnyWeatherNetWork.getRealtimeWeather(lng,lat)
                }
                val deferredDaily=async {
                    SunnyWeatherNetWork.getDailyWeather(lng,lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if(realtimeResponse.status =="ok"&& dailyResponse.status=="ok"){
                    val weather = Weather(realtimeResponse.result.realtime,
                        dailyResponse.result.daily)
                    Result.success(weather)
                }else{
                    Result.failure(RuntimeException(
                        "realtime response status is ${realtimeResponse.status}"+
                        "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
//        }catch (e:Exception){
//            Result.failure<Weather>(e)
//        }
//        emit(result)
    }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace()=PlaceDao.getSavedPlace()

    fun isPlaceSaved()=PlaceDao.isPlaceSaved()

    //高阶函数技巧，把入口函数封装，使得之后的网络请求代码就不用再写try - catch语句
    private fun <T>fire(context: CoroutineContext,block:suspend ()->Result<T>)=
        liveData<Result<T>>(context){
            val result = try {
                block()
            }catch (e:Exception){
                Result.failure<T>(e)
            }
            emit(result)
        }
}