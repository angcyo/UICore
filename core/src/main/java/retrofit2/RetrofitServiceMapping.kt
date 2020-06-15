package retrofit2

import android.text.TextUtils
import androidx.collection.ArrayMap
import com.angcyo.http.BuildConfig
import java.lang.reflect.Method

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/06
 */
object RetrofitServiceMapping {
    /**
     * 功能总开关
     */
    @JvmField
    var enableMapping = BuildConfig.DEBUG

    /**
     * 当方法名匹配不到url时, 是否使用声明的url匹配映射的url.
     *
     *
     * 关闭之后, 将只匹配方法名对应的url
     */
    var enableUrlMap = true

    /**
     * 映射关系表, key 可以是方法名, 也是部分url字符串
     */
    @JvmField
    var defaultMap: Map<String, String> =
        ArrayMap()

    @JvmStatic
    fun init(enableMapping: Boolean, methodMapping: Map<String, String>) {
        RetrofitServiceMapping.enableMapping = enableMapping
        defaultMap = methodMapping
    }

    /**
     * 请在调用
     * <pre>
     * Retrofit.create()
    </pre> *
     * 之前调用.
     *
     *
     * 比如:
     * Retrofit.create()
     * 替换成
     * RetrofitServiceMapping.mapping().create()
     *
     *
     * 暂不支持 Retrofit 的单例模式.
     */
    fun mapping(retrofit: Retrofit, service: Class<*>): Retrofit {
        if (defaultMap != null && !defaultMap.isEmpty() && enableMapping) {
            configRetrofit(
                retrofit,
                service,
                defaultMap
            )
        } else {
            try {
                val serviceMethodCache =
                    Reflect.getMember(
                        retrofit,
                        "serviceMethodCache"
                    ) as MutableMap<Method, ServiceMethod<*>>
                serviceMethodCache.clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return retrofit
    }

    private fun configRetrofit(
        retrofit: Retrofit,
        service: Class<*>,
        map: Map<String, String>
    ) {
        for (method in service.declaredMethods) {
            try {
                var mapUrl = map[method.name]
                val serviceMethod: ServiceMethod<Any> =
                    ServiceMethod.parseAnnotations(retrofit, method)
                var requestFactory: RequestFactory? = null
                if (serviceMethod is HttpServiceMethod<*, *>) {
                    requestFactory = Reflect.getMember(
                        HttpServiceMethod::class.java,
                        serviceMethod,
                        "requestFactory"
                    ) as RequestFactory
                }
                if (TextUtils.isEmpty(mapUrl) && enableUrlMap) {
                    //通过方法, 拿不到映射的url时, 则匹配url映射
                    if (requestFactory != null) {
                        val relativeUrl = Reflect.getFieldValue(
                            requestFactory,
                            "relativeUrl"
                        ) as String
                        if (!TextUtils.isEmpty(relativeUrl)) {
                            for (key in map.keys) {
                                val url = map[key]
                                if (relativeUrl.contains(key)) {
                                    mapUrl = url
                                    break
                                }
                            }
                        }
                    }
                } else {
                    //retrofit 2.4
                    //ServiceMethod.Builder methodBuilder = new ServiceMethod.Builder(retrofit, method);
                    //ServiceMethod serviceMethod = methodBuilder.build();
                    //Reflect.setFieldValue(serviceMethod, "relativeUrl", mapUrl);
                    //Map<Method, ServiceMethod> serviceMethodCache = (Map<Method, ServiceMethod>) Reflect.getMember(retrofit, "serviceMethodCache");
                    //serviceMethodCache.put(method, serviceMethod);
                    //end

                    //Log.i("angcyo", "succeed");
                }
                if (requestFactory != null && !TextUtils.isEmpty(mapUrl)) {
                    Reflect.setFieldValue(requestFactory, "relativeUrl", mapUrl)
                    val serviceMethodCache =
                        Reflect.getMember(
                            retrofit,
                            "serviceMethodCache"
                        ) as MutableMap<Method, ServiceMethod<*>>
                    serviceMethodCache[method] = serviceMethod
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}