package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetWork
import kotlinx.coroutines.Dispatchers
import java.lang.RuntimeException
import kotlin.Exception

//仓库类，用于判断调用方请求的数据应该从本地数据源中获取还是从网络源中获取，并将获取的数据返回调用方，本应用只采用网络源
object Repository {
    //Dispatchers.Default/IO/Main,三种分别为不同的线程参数，三者分别为低并发，较高并发，（不开启子线程，只执行主线程
//    此处线程参数指定为Dispatchers.IO,代表代码块中的所有代码都能运行在子线程中了，Android不允许主线程进行网络请求
    fun searchPlaces(query:String)= liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetWork.searchPlaces(query)
            if(placeResponse.status == "ok"){
                val places = placeResponse.places
                //内置函数，返回包装好的数据列表
                Result.success(places)
            }else{
                //包装异常信息
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e:Exception){
            Result.failure<List<Place>>(e)
        }
        //发射结果
        emit(result)
    }
}