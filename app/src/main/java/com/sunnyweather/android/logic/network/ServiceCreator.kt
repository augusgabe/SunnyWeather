package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {
    //访问该服务器域
    private const val BASE_URL = "https://api.caiyunapp.com"
    //创建网络库，来连接服务器
    private val retrofit = Retrofit.Builder()
//            指定基础服务器域名
        .baseUrl(BASE_URL)
//            指定解析时使用的数据转换库，将数据转换为json文件
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    //外部create方法调用，创建相应Service接口动态代理对象
    fun <T>create(serviceClass: Class<T>):T = retrofit.create(serviceClass)
//    inline,reified关键字将该方法变为内联函数，修饰泛型，将泛型实化,使得create能直接返回指定泛型的实际类型
//    原本写法：val appService =ServiceCreator.create(AppService::class.java)
//    泛型实化后：val appService =ServiceCreator.create<AppService>()
    inline fun <reified T>create():T = create(T::class.java)
}