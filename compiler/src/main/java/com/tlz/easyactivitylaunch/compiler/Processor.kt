package com.tlz.easyactivitylaunch.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion

/**
 * Created by tomlezen.
 * Data: 2017/12/14.
 * Time: 14:17.
 */
@AutoService(Processor::class)
class Processor : BasicAnnotationProcessor() {

    override fun initSteps(): MutableIterable<ProcessingStep> = mutableListOf(Step(processingEnv))

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
}