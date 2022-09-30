package com.activeminds.projectrepo.utils

/**
 * Provides a [ViewDataBinding] object of the declared type by
 * calling [DataBindingUtil.setContentView] with the property owning [Activity] and the specified
 * layout resource id
 */

import android.os.Looper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(noinline initializer: (LayoutInflater) -> T) =
    ViewBindingPropertyDelegate(this, initializer)

class ViewBindingPropertyDelegate<T : ViewBinding>(
    private val activity: AppCompatActivity,
    private val initializer: (LayoutInflater) -> T
) : ReadOnlyProperty<AppCompatActivity, T>, DefaultLifecycleObserver {

    private var _value: T? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    @Suppress("Unused")
    fun onCreate() {
        if (_value == null) {
            _value = initializer(activity.layoutInflater)
        }
        activity.setContentView(_value?.root!!)
        activity.lifecycle.removeObserver(this)
    }

    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        if (_value == null) {

            // This must be on the main thread only
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw IllegalThreadStateException("This cannot be called from other threads. It should be on the main thread only.")
            }

            _value = initializer(thisRef.layoutInflater)
        }
        return _value!!
    }
}