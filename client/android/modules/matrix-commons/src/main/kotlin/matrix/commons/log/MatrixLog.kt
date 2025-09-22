/*
 * Copyright (C) Inswave Systems, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Inswave Systems <webmaster@inswave.com>, 2018
 */

package matrix.commons.log

import android.util.Log


/**
 * Created by sangjun002 on 2017. 8. 2..
 * Websquare Hybrid Log를 위한 Class
 * 일단은 기본 Log 출력
 *
 * Modified 2021.09.15 - tarkarn
 * Class, Method, LineNumber 나오도록 추가.
 *
 * Modified 2022.06.30 - tarkarn
 * prefix parameter 받을 수 있도록 추가.
 *
 * ex.
 *  MatrixLog.d(tag = "tag", prefix = "prefix", value = "msg")
 *
 * output
 *  tag: \[prefix\] msg
 */

object MatrixLog {

    private var loggable = false

    fun init(debug: Boolean) {
        loggable = debug
    }

    /*********************
     * LogLevel: Debug *
     *********************/
    @JvmStatic
    fun d(value: String){
        if (loggable) Log.d(getTag(), message(value))
    }

    @JvmStatic
    fun d(tag: String, value: String){
        if (loggable) Log.d(getTag(), message(tag, value))
    }

    @JvmStatic
    fun d(tag: String, prefix: String, value: String){
        if (loggable) Log.d(getTag(), message(tag, "[$prefix] $value"))
    }

    /*********************
     * LogLevel: Verbose *
     *********************/
    @JvmStatic
    fun v(value : String){
        if (loggable) Log.v(getTag(), message(value))
    }

    @JvmStatic
    fun v(tag: String, value: String){
        if (loggable) Log.v(getTag(), message(tag, value))
    }

    @JvmStatic
    fun v(tag: String, prefix: String, value: String){
        if (loggable) Log.v(getTag(), message(tag, "[$prefix] $value"))
    }


    /*******************
     * LogLevel: Error *
     *******************/
    @JvmStatic
    fun e(value : String?){
        if (loggable) Log.e(getTag(), message(value ?: ""))
    }

    @JvmStatic
    fun e(t: Throwable) {
        if (loggable) Log.e(getTag(), message(t.message ?: ""), t)
    }

    @JvmStatic
    fun e(value: String?, t: Throwable) {
        if (loggable) Log.e(getTag(), message(value ?: ""), t)
    }

    @JvmStatic
    fun e(tag: String, value: String){
        if (loggable) Log.e(getTag(), message(tag, value))
    }

    @JvmStatic
    fun e(tag: String, prefix: String, value: String){
        if (loggable) Log.e(getTag(), message(tag, "[$prefix] $value"))
    }

    @JvmStatic
    fun e(tag: String, value: String, t: Throwable?) {
        if (loggable) Log.e(getTag(), message(tag, value), t)
    }

    @JvmStatic
    fun e(tag: String, prefix: String, value: String, t: Throwable?){
        if (loggable) Log.e(getTag(), message(tag, "[$prefix] $value"), t)
    }

    /******************
     * LogLevel: Info *
     ******************/
    @JvmStatic
    fun i(value : String){
        if(loggable) Log.i(getTag(), message(value))
    }

    @JvmStatic
    fun i(tag : String, value : String){
        if(loggable) Log.i(getTag(), message(tag, value))
    }

    @JvmStatic
    fun i(tag: String, prefix: String, value: String){
        if(loggable) Log.i(getTag(), message(tag, "[$prefix] $value"))
    }


    /******************
     * LogLevel: Warn *
     ******************/
    @JvmStatic
    fun w(value : String){
        if(loggable) Log.w(getTag(), message(value))
    }

    @JvmStatic
    fun w(tag : String, value : String){
        if(loggable) Log.w(getTag(), message(tag, value))
    }

    @JvmStatic
    fun w(tag: String, prefix: String, value: String){
        if(loggable) Log.w(getTag(), message(tag, "[$prefix] $value"))
    }


    /***************************
     * LogLevel: Warn (System) *
     * Debuggable 체크하지 않음   *
     ***************************/
    @JvmStatic
    fun system(value: String) {
        Log.w(getTag(), message(value))
    }

    @JvmStatic
    fun system(value: String, t: Throwable) {
        Log.e(getTag(), message(value), t)
    }


    private fun message(message: String): String = "[${getThreadName()}] $message"
    private fun message(customTag: String, message: String): String = "[$customTag][${getThreadName()}] $message"
    private fun getThreadName(): String = Thread.currentThread().name
    private fun getLineNumber(): Int = Thread.currentThread().stackTrace[5].lineNumber
    private fun getClassName(): String = Thread.currentThread().stackTrace[5].fileName ?: ""
    private fun getTag(): String = "${getClassName()}:${getLineNumber()}"
}