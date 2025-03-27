package com.example.trackify.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.trackify.data.RunEntity

@Database
    (entities = [RunEntity::class],
     version = 1,
     exportSchema = false)
abstract class RunDatabase : RoomDatabase() {

    abstract fun getRunDAO(): RunDAO

}

//Dependency is kotlin object / variable
// Injection is the process of providing a dependency to a dependent object.
// Dependency Injection is a design pattern that removes the dependency from the programming code so that it can be easy to manage and test.
// Dagger is a fully static, compile-time dependency injection framework for both Java and Android. It is an adaptation of an earlier version created by Square and now maintained by Google.
// Koin is a pragmatic lightweight dependency injection framework for Kotlin developers. It is written in pure Kotlin using functional resolution only: no proxy, no code generation, no reflection!