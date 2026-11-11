package com.yosrhammami.daggerlab.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
            ?: error("Unknown ViewModel class: ${modelClass.name}. Did you add it to ViewModelModule?")
        @Suppress("UNCHECKED_CAST")
        return creator.get() as T
    }
}