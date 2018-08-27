package com.tlz.easyactivitylaunch.compiler.model

import com.tlz.easyactivitylaunch.EasyLaunch
import com.tlz.easyactivitylaunch.EasyLaunch1
import com.tlz.easyactivitylaunch.EasyLaunch2
import com.tlz.easyactivitylaunch.EasyLaunchForResult
import com.tlz.easyactivitylaunch.compiler.pkg
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

/**
 * Created by Tomlezen.
 * Data: 2018/8/27.
 * Time: 10:48.
 */
class EasyLaunchModel(val elementsByAnnotation: List<Pair<Class<out Annotation>, Element>> = mutableListOf()) {
    val pkg = EasyLaunch::class.java.`package`.name
    val className = "EasyActivityLaunchExt"
}