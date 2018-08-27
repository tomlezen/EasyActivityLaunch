package com.tlz.easyactivitylaunch.compiler.writer

import com.squareup.kotlinpoet.FileSpec
import com.tlz.easyactivitylaunch.compiler.model.EasyLaunchModel
import java.io.File
import javax.annotation.processing.Messager

/**
 * Created by Tomlezen.
 * Data: 2018/8/27.
 * Time: 11:25.
 */
class InitializerWriter(private val model: EasyLaunchModel, private val messager: Messager) {
    fun write(location: File) {
        FileSpec.builder(model.pkg, model.className)
                .apply { InitializerGenerator(this, messager).create(model) }
                .build()
                .writeTo(location)
    }
}