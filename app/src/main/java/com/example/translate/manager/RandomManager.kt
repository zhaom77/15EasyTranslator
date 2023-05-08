package com.example.translate.manager

import java.util.*

object RandomManager {


    /**
     * 生成从1 ~ 100的随机数
     */
    fun random(): Int {
        val random = Random()
        return random.nextInt(100) + 1
    }

    /**
     * 生成从1 ~ num的随机数
     */
    fun random(num: Int): Int {
        if (num <= 0) {
            return 1
        }
        val random = Random()
        return random.nextInt((num)) + 1
    }

}