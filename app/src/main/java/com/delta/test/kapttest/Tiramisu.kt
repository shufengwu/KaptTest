package com.delta.test.kapttest

import com.example.Factory

/**
 * Created by Shufeng.Wu on 2017/8/16.
 */
@Factory(id = "Tiramisu")
class Tiramisu : Meal {
    override val price: Float
        get() = 0.3f
}