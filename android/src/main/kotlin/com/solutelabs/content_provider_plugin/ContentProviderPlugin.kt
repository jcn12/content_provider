package com.solutelabs.content_provider_plugin

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Suppress("MoveVariableDeclarationIntoWhen", "UNCHECKED_CAST")
class ContentProviderPlugin() : MethodCallHandler {
    var activity: Activity? = null
    var channel: MethodChannel? = null
    var compositeDisposable = CompositeDisposable()
    private var activityLifecycleCallbacks: ActivityLifecycleCallbacks? = null



    constructor(activity: Activity, channel: MethodChannel) : this() {
        this.activity = activity
        this.channel = channel
        this.channel?.setMethodCallHandler(this)

        this.activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {

            }

            override fun onActivityResumed(activity: Activity?) {

            }

            override fun onActivityStarted(activity: Activity?) {

            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

            }

            override fun onActivityStopped(activity: Activity?) {

            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

            }

            override fun onActivityDestroyed(activity: Activity?) {
                compositeDisposable.dispose()
            }

        }
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_plugin")
            channel.setMethodCallHandler(ContentProviderPlugin(registrar.activity(), channel))
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {


        Log.d("TAG",call.method + call.arguments.javaClass);
        when (call.method) {
            "getContent" -> {
                val map = call.arguments as java.util.HashMap<String, String>
                var query: Cursor? = null
                Observable.fromCallable {
                    query = activity?.contentResolver?.query(Uri.parse(map["uri"]), null, null, null, null)
                    query
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            it?.apply {
                                val data = mutableListOf<HashMap<String, Any>>()
                                if (this.moveToFirst()) {
                                    do {
                                        val value = HashMap<String, Any>()
                                        for (index in 0..it.columnCount) {
                                            when (it.getType(index)) {
                                                /*int */1 -> value[it.getColumnName(index)] = it.getInt(index)
                                                /*float */ 2 -> value[it.getColumnName(index)] = it.getFloat(index)
                                                /*string */3 -> value[it.getColumnName(index)] = it.getString(index)
                                                /*blob */4 -> value[it.getColumnName(index)] = it.getBlob(index)
                                                else -> {
                                                }
                                            }
                                        }
                                        data.add(value)
                                    } while (moveToNext())
                                }
                                result.success(data.toList())
                                query?.close()
                            }
                        }, {
                            it.printStackTrace()
                            result.error("1", it.message, "")
                            query?.close()
                        }).apply {
                            compositeDisposable.add(this)
                        }
            }

            "insertContent" -> {
                Log.d("TAG",call.arguments.toString() + call.arguments.javaClass);
                val uri = call.argument<String>("uri")
                Observable.fromCallable {
                    activity?.contentResolver?.insert(Uri.parse(uri), getContentValues(call, result))
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            result.success(it)
                        }, {
                            it.printStackTrace()
                            result.error("1", it.message, "")
                        }).apply {
                            compositeDisposable.add(this)
                        }

            }

            "updateContent" -> {
                val uri = call.argument<String>("uri")
                val where = call.argument<String>("where")
                val whereArgs = call.argument<Array<String>>("whereArgs")

                Observable.fromCallable {
                    activity?.contentResolver?.update(Uri.parse(uri), getContentValues(call, result), where, whereArgs)
                }.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            result.success(it)
                        }, {
                            it.printStackTrace()
                            result.error("1", it.message, "")
                        }).apply {
                            compositeDisposable.add(this)
                        }
            }

            "deleteContent" -> {
                val uri = call.argument<String>("uri")
                val where = call.argument<String>("where")
                val selectionArgs = call.argument<Array<String>>("selectionArgs")
                Observable.fromCallable {
                    activity?.contentResolver?.delete(Uri.parse(uri), where, selectionArgs)
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            result.success(it)
                        }, {
                            it.printStackTrace()
                            result.error("1", it.message, "")
                        }).apply {
                            compositeDisposable.add(this)
                        }
            }
            else -> result.notImplemented()
        }
    }

    private fun getContentValues(call: MethodCall, result: Result): ContentValues {
        val values = call.argument<HashMap<String, Any>>("contentValues")
        val contentValues = ContentValues()

        values?.forEach {
            val value = it.value
            when (value) {
                is String -> contentValues.put(it.key, value)
                is Byte -> contentValues.put(it.key, value)
                is Short -> contentValues.put(it.key, value)
                is Int -> contentValues.put(it.key, value)
                is Long -> contentValues.put(it.key, value)
                is Float -> contentValues.put(it.key, value)
                is Double -> contentValues.put(it.key, value)
                is Boolean -> contentValues.put(it.key, value)
                is ByteArray -> contentValues.put(it.key, value)
                else -> result.error("unknown type", "", "")
            }
        }
        return contentValues
    }

}
