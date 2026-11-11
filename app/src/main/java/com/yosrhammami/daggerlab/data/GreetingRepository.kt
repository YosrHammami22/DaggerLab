package com.yosrhammami.daggerlab.data

import javax.inject.Inject
import javax.inject.Singleton

interface GreetingRepository {
    fun getName(): String
}

@Singleton
class GreetingRepositoryImpl @Inject constructor() : GreetingRepository {
    override fun getName(): String = "Dagger"
}
