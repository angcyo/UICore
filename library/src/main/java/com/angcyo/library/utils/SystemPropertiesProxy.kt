package com.angcyo.library.utils

import android.annotation.SuppressLint
import android.content.Context

/**
 * @author [angcyo](mailto:angcyo@126.com)
 * @since 2022/12/13
 *
 * https://github.com/Arupakaman-Studios/SOSApp-Lite
 */
@SuppressLint("PrivateApi")
object SystemPropertiesProxy {

    /**
     * Get the value for the given key.
     *
     * @return an empty string if the key isn't found
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    operator fun get(context: Context, key: String?): String? {
        var ret: String? = null
        try {
            val cl = context.classLoader
            val clazz = cl.loadClass("android.os.SystemProperties")

            // Parameters Types
            val paramTypes: Array<Class<*>?> = arrayOfNulls(1)
            paramTypes[0] = String::class.java
            val get = clazz.getMethod("get", *paramTypes)

            // Parameters
            val params = arrayOfNulls<Any>(1)
            params[0] = key
            ret = get.invoke(clazz, *params) as String
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            ret = null
        } catch (e: Exception) {
            ret = null
        }
        return ret
    }

    /**
     * Get the value for the given key.
     *
     * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    operator fun get(context: Context, key: String?, def: String?): String? {
        var ret = def
        try {
            val cl = context.classLoader
            val clazz = cl.loadClass("android.os.SystemProperties")

            // Parameters Types
            val paramTypes: Array<Class<*>?> = arrayOfNulls(2)
            paramTypes[0] = String::class.java
            paramTypes[1] = String::class.java
            val get = clazz.getMethod("get", *paramTypes)

            // Parameters
            val params = arrayOfNulls<Any>(2)
            params[0] = key
            params[1] = def
            ret = get.invoke(clazz, *params) as String
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            ret = def
        } catch (e: Exception) {
            ret = def
        }
        return ret
    }

    /**
     * Get the value for the given key, and return as an integer.
     *
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as an integer, or def if the key isn't found or cannot be parsed
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    fun getInt(context: Context, key: String?, def: Int): Int {
        var ret = def
        try {
            val cl = context.classLoader
            val clazz = cl.loadClass("android.os.SystemProperties")

            // Parameters Types
            val paramTypes = arrayOfNulls<Class<*>?>(2)
            paramTypes[0] = String::class.java
            paramTypes[1] = Int::class.javaPrimitiveType
            val getInt = clazz.getMethod("getInt", *paramTypes)

            // Parameters
            val params = arrayOfNulls<Any>(2)
            params[0] = key
            params[1] = def
            ret = getInt.invoke(clazz, *params) as Int
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            ret = def
        } catch (e: Exception) {
            ret = def
        }
        return ret
    }

    /**
     * Get the value for the given key, and return as a long.
     *
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a long, or def if the key isn't found or cannot be parsed
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    fun getLong(context: Context, key: String?, def: Long): Long {
        var ret = def
        try {
            val cl = context.classLoader
            val clazz = cl.loadClass("android.os.SystemProperties")

            // Parameters Types
            val paramTypes = arrayOfNulls<Class<*>?>(2)
            paramTypes[0] = String::class.java
            paramTypes[1] = Long::class.javaPrimitiveType
            val getLong = clazz.getMethod("getLong", *paramTypes)

            // Parameters
            val params = arrayOfNulls<Any>(2)
            params[0] = key
            params[1] = def
            ret = getLong.invoke(clazz, *params) as Long
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            ret = def
        } catch (e: Exception) {
            ret = def
        }
        return ret
    }

    /**
     * Get the value for the given key, returned as a boolean. Values 'n', 'no', '0', 'false' or
     * 'off' are considered false. Values 'y', 'yes', '1', 'true' or 'on' are considered true. (case
     * insensitive). If the key does not exist, or has any other value, then the default result is
     * returned.
     *
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or is not able to be
     * parsed as a boolean.
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    fun getBoolean(context: Context, key: String?, def: Boolean): Boolean {
        var ret = def
        try {
            val cl = context.classLoader
            val clazz = cl.loadClass("android.os.SystemProperties")

            // Parameters Types
            val paramTypes = arrayOfNulls<Class<*>?>(2)
            paramTypes[0] = String::class.java
            paramTypes[1] = Boolean::class.javaPrimitiveType
            val getBoolean = clazz.getMethod("getBoolean", *paramTypes)

            // Parameters
            val params = arrayOfNulls<Any>(2)
            params[0] = key
            params[1] = def
            ret = getBoolean.invoke(clazz, *params) as Boolean
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            ret = def
        } catch (e: Exception) {
            ret = def
        }
        return ret
    }

/*
    / **
    * Set the value for the given key.
    * @throws IllegalArgumentException if the key exceeds 32 characters
    * @throws IllegalArgumentException if the value exceeds 92 characters
    */
    /*
    public static void set(Context context, String key, String val) throws IllegalArgumentException {

        try{

            @SuppressWarnings("unused")
            DexFile df = new DexFile(new File("/system/app/Settings.apk"));
            @SuppressWarnings("unused")
            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = Class.forName("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes= new Class[2];
            paramTypes[0]= String.class;
            paramTypes[1]= String.class;

            Method set = SystemProperties.getMethod("set", paramTypes);

            //Parameters
            Object[] params= new Object[2];
            params[0]= key;
            params[1]= val;

            set.invoke(SystemProperties, params);

        }catch( IllegalArgumentException iAE ){
            throw iAE;
        }catch( Exception e ){
        }

    }*/
}