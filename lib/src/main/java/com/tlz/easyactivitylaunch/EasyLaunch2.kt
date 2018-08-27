package com.tlz.easyactivitylaunch

import kotlin.reflect.KClass

/**
 * 创建launch代码注解，所用与activity上.
 *
 * Created by Tomlezen.
 * Data: 2018/8/27.
 * Time: 10:06.
 *
 * @param nickName 方法别名.
 * @param parameters 参数.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EasyLaunch2(val nickName: String = "", val parameters: Array<KClass<out Any>> = [])