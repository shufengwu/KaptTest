package com.delta.test.kapttest

import com.example.Factory

/**
 * Created by Shufeng.Wu on 2017/8/16.
 */

@Factory(id = "Calzone")
class CalzonePizza : Meal {
    override val price: Float
        get() = 0.2f
}