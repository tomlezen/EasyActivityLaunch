package com.tlz.easyactivitylaunch.compiler.model

import com.tlz.easyactivitylaunch.EasyLaunch
import javax.lang.model.element.Element

/**
 * Created by Tomlezen.
 * Data: 2018/8/27.
 * Time: 10:48.
 */
class EasyLaunchModel(val elementsByAnnotation: List<Pair<Class<out Annotation>, Element>> = mutableListOf()) {
    val pkg = EasyLaunch::class.java.`package`.name
    val className = "EasyActivityLaunchExt"
}