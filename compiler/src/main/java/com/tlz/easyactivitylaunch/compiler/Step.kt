package com.tlz.easyactivitylaunch.compiler

import android.app.Activity
import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.SetMultimap
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmWildcard
import com.tlz.easyactivitylaunch.EasyLaunch
import com.tlz.easyactivitylaunch.EasyLaunch1
import com.tlz.easyactivitylaunch.EasyLaunch2
import com.tlz.easyactivitylaunch.EasyLaunchForResult
import com.tlz.easyactivitylaunch.compiler.model.EasyLaunchModel
import com.tlz.easyactivitylaunch.compiler.writer.InitializerWriter
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * Created by tomlezen.
 * Data: 2017/12/14.
 * Time: 14:32.
 */
class Step(private val processingEnv: ProcessingEnvironment) : BasicAnnotationProcessor.ProcessingStep {

    /**
     * 源码生成位置.
     */
    private val sourceLocation by lazy {
        val infoFile = processingEnv.filer.createSourceFile("package-info", null)
        val out = infoFile.openWriter()
        out.close()
        File(infoFile.name).parentFile
    }

    private val messager = processingEnv.messager

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>?): MutableSet<Element> {
        val deferredTypes = mutableSetOf<Element>()
//        try {
            elementsByAnnotation?.let {
                val newElementsByAnnotation = mutableListOf<Pair<Class<out Annotation>, Element>>()
                it.forEach { clazz, element ->
                    // 检查是否Activity子类
                    if (element.asType().isSubtypeOfType(Activity::class.java.canonicalName)) {
                        newElementsByAnnotation.add(clazz to element)
                    } else {
                        messager.printMessage(Diagnostic.Kind.WARNING, "${element.asType()} is not Activity sub class")
                    }
                }
                InitializerWriter(EasyLaunchModel(newElementsByAnnotation), messager).write(sourceLocation)
            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            messager.printMessage(Diagnostic.Kind.ERROR, e.message)
//        }
        return deferredTypes
    }

    override fun annotations(): MutableSet<out Class<out Annotation>> {
        val set = HashSet<Class<out Annotation>>()
        set.add(EasyLaunch::class.java)
        set.add(EasyLaunch1::class.java)
        set.add(EasyLaunch2::class.java)
        set.add(EasyLaunchForResult::class.java)
        return set
    }

}