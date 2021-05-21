package com.bihe0832.android.lib.ipc

import android.app.Application
import android.net.Uri
import android.os.IInterface
import com.bihe0832.android.lib.ipc.annotation.Process
import com.bihe0832.android.lib.ipc.annotation.SupportMultiProcess
import com.bihe0832.android.lib.ipc.iservice.IBinderProvider
import com.bihe0832.android.lib.ipc.iservice.IService
import com.bihe0832.android.lib.ipc.iservice.ServiceModel
import com.bihe0832.android.lib.log.ZLog
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by hardyshi on 10/28/2020.
 */
object ServiceManager {

    private val serviceMap = ConcurrentHashMap<String, ServiceModel>()

    private val processList = mutableListOf<String>()

    private val proxyMap = mutableMapOf<Class<out IService>, Any>()

    private var mApplication: Application? = null

    fun attach(binderProvider: IBinderProvider) {
        binderProvider.attach { classname ->
            ZLog.d("ServiceManager", "binderPool getBinder:$classname $serviceMap")
            serviceMap[classname]!!.service.binder!!
        }
    }

    fun initApplication(application: Application) {
        mApplication = application
    }

    fun registerProcess(process: String) {
        processList.add(process)
    }

    fun <T : IService> registerService(clazz: Class<T>, serviceImplement: T) {
        ZLog.d("ServiceManager", "registerService invoke, $clazz $serviceImplement ${clazz.annotations.any { it is SupportMultiProcess }}")

        serviceMap[clazz.name] =
                ServiceModel(serviceImplement, clazz.annotations.any { it is SupportMultiProcess })
    }

    fun <T : IService> getService(serviceClass: Class<T>): T? {
        val serviceModel = serviceMap[serviceClass.name]!!
        return if (serviceModel.isSupportMultiProcess) {
            try {
                generateProxy(
                        serviceClass,
                        serviceModel.service as T
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            serviceModel.service as T
        }
    }

    private fun <T : IService> generateProxy(clazz: Class<T>, service: T): T? {
        try {
            return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) { proxy, method, args ->
                val binderAnnotation = method.getAnnotation(Process::class.java)
                if (binderAnnotation != null) {
                    if (processList.any { processName -> processName == binderAnnotation.name }) {
                        val binderProxy = proxyMap[clazz] ?: getProxy(
                                binderAnnotation.name,
                                clazz,
                                service
                        ).also {
                            if (null != it) {
                                proxyMap[clazz] = it
                            }
                        }

                        ZLog.d("ServiceManager", "binderProxy:$binderProxy")
                        binderProxy?.let { binderProxyNotNull ->
                            val binderMethod = binderProxyNotNull.javaClass.getMethod(method.name, *method.parameterTypes)
                            invokeMethod(
                                    binderProxyNotNull,
                                    binderMethod,
                                    args
                            )
                        }
                    } else {
                        invokeMethod(
                                service,
                                method,
                                args
                        )
                    }
                } else {
                    invokeMethod(
                            service,
                            method,
                            args
                    )
                }
            } as T
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getProxy(process: String, serviceClazz: Class<out IService>, service: IService): IInterface? {

        var uri = "content://" + mApplication!!.packageName + ".process_dispatcher_${process}"
        ZLog.d("ServiceManager", "getProxy:$uri")
        try {
            val bundle = mApplication!!.contentResolver.call(
                    Uri.parse(uri),
                    "getBinder",
                    serviceClazz.name,
                    null
            )
            ZLog.d("ServiceManager", "getProxy:$bundle")
            return service.getInterface(bundle?.getBinder("binder"))!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun invokeMethod(obj: Any, method: Method, args: Array<Any>?): Any? {
        return if (args.isNullOrEmpty()) {
            method.invoke(obj)
        } else {
            method.invoke(obj, *args)
        }
    }
}