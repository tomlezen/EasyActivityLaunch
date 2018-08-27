package com.tlz.easyactivitylaunch.compiler.writer

import android.app.Activity
import android.content.Intent
import com.squareup.kotlinpoet.*
import com.sun.tools.javac.code.Attribute
import com.tlz.easyactivitylaunch.EasyLaunch
import com.tlz.easyactivitylaunch.EasyLaunch1
import com.tlz.easyactivitylaunch.EasyLaunch2
import com.tlz.easyactivitylaunch.EasyLaunchForResult
import com.tlz.easyactivitylaunch.compiler.firstUpperCase
import com.tlz.easyactivitylaunch.compiler.getAnnotationMirror
import com.tlz.easyactivitylaunch.compiler.getAnnotationValue
import com.tlz.easyactivitylaunch.compiler.model.EasyLaunchModel
import com.tlz.easyactivitylaunch.compiler.typeName
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleTypeVisitor7
import javax.tools.Diagnostic

/**
 * Created by Tomlezen.
 * Data: 2018/8/27.
 * Time: 11:26.
 */
class InitializerGenerator(private val builder: FileSpec.Builder, private val messager: Messager) {

    fun create(model: EasyLaunchModel) {
        createLaunchFunctions(model).forEach { builder.addFunction(it) }
    }

    /**
     * 创建启动方法.
     * @param model EasyLaunchModel
     * @return List<FunSpec>
     */
    private fun createLaunchFunctions(model: EasyLaunchModel): List<FunSpec> {
        val functions = mutableListOf<FunSpec>()
        // 遍历所有的注解，并检车注解的class是否是继承于Activity
        model.elementsByAnnotation.forEach {
            val (clazz, element) = it
            if (clazz.canonicalName == EasyLaunchForResult::class.java.canonicalName) {
                functions.add(createLaunchForResultFunction(clazz, element))
            } else {
                functions.add(createLaunchFunction(clazz, element))
            }
        }
        return functions
    }

    /**
     * 创建启动方法.
     * @param clazz Class<out Any>
     * @param element Element
     * @return FunSpec
     */
    private fun createLaunchFunction(clazz: Class<out Any>, element: Element): FunSpec {
        val nickName = when (clazz) {
            EasyLaunch::class.java -> {
                val annotation = element.getAnnotation(EasyLaunch::class.java)
                annotation.nickName
            }
            EasyLaunch1::class.java -> {
                val annotation = element.getAnnotation(EasyLaunch::class.java)
                annotation.nickName
            }
            EasyLaunch2::class.java -> {
                val annotation = element.getAnnotation(EasyLaunch::class.java)
                annotation.nickName
            }
            else -> ""
        }
        val actName = if (nickName.isEmpty()) element.asType().toString().split(".").last() else nickName.firstUpperCase()
        return FunSpec.builder("launch$actName")
                .receiver(Activity::class)
                .addKdoc(getKDocCodeBlock(actName))
                .apply {
                    val (parameters, codeBlock) = createParametersAndBody(clazz, element as TypeElement)
                    addParameters(parameters)
                    addCode(codeBlock)
                }
                .build()
    }

    /**
     * 创建启动方法.
     * @param clazz Class<out Any>
     * @param element Element
     * @return FunSpec
     */
    private fun createLaunchForResultFunction(clazz: Class<out Any>, element: Element): FunSpec {
        val actName = element.asType().toString().split(".").last()
        return FunSpec.builder("launch${actName}ForResult")
                .receiver(Activity::class)
                .addKdoc(getKDocCodeBlock(actName))
                .apply {
                    val (parameters, codeBlock) = createParametersAndBody(clazz, element as TypeElement)
                    addParameters(parameters)
                    addCode(codeBlock)
                }
                .build()
    }

    private fun TypeElement.getParameters(clazz: Class<out Any>): List<TypeMirror> =
            (getAnnotationMirror(clazz)?.getAnnotationValue("parameters") as? Attribute.Array)?.values?.mapTo(mutableListOf()) { (it as Attribute.Class).classType } ?: listOf()

    private fun TypeElement.getParameterNames(clazz: Class<out Any>): List<String> =
            (getAnnotationMirror(clazz)?.getAnnotationValue("parameterNames") as? Attribute.Array)?.values?.mapTo(mutableListOf()) {
                it.toString().run {
                    substring(1, length - 1)
                }
            } ?: listOf()

    /**
     * 生成参数，和方法块.
     * @param element TypeElement
     * @return List<ParameterSpec>
     */
    private fun createParametersAndBody(clazz: Class<out Any>, element: TypeElement): Pair<List<ParameterSpec>, CodeBlock> {
        val parameterSpecs = mutableListOf<ParameterSpec>()
        val codeBlockBuilder = CodeBlock.builder()
                .addStatement("val intent = %T(this, %T::class.java)", Intent::class, element)
        val (parameters, parameterNames) = when (clazz) {
            EasyLaunchForResult::class.java -> {
                parameterSpecs.add(ParameterSpec.builder("requestCode", Int::class).build())
                element.getParameters(EasyLaunchForResult::class.java) to element.getParameterNames(EasyLaunchForResult::class.java)
            }
            EasyLaunch::class.java -> {
                element.getParameters(EasyLaunch::class.java) to element.getParameterNames(EasyLaunch::class.java)
            }
            EasyLaunch1::class.java -> {
                element.getParameters(EasyLaunch1::class.java) to element.getParameterNames(EasyLaunch1::class.java)
            }
            EasyLaunch2::class.java -> {
                element.getParameters(EasyLaunch2::class.java) to element.getParameterNames(EasyLaunch2::class.java)
            }
            else -> listOf<TypeMirror>() to listOf()
        }
        if (parameterNames.isEmpty() && parameters.isNotEmpty()) {
            parameters.mapIndexedTo(parameterSpecs) { index, kClass ->
                val paramName = "param${index + 1}"
                codeBlockBuilder.addStatement(getPutMethod(paramName))
                ParameterSpec.builder(paramName, kClass.typeName()).build()
            }
        } else if (parameterNames.isNotEmpty() && parameters.size != parameterNames.size) {
            messager.printMessage(Diagnostic.Kind.ERROR, "parameter size is ${parameters.size}, but parameterName size is ${parameterNames.size}")
        } else {
            parameters.mapIndexedTo(parameterSpecs) { index, kClass ->
                val paramName = parameterNames[index]
                codeBlockBuilder.addStatement(getPutMethod(paramName))
                ParameterSpec.builder(paramName, kClass.typeName()).build()
            }
        }

        codeBlockBuilder.apply {
            if (clazz == EasyLaunchForResult::class.java) {
                addStatement("startActivityForResult(intent, requestCode)")
            } else {
                addStatement("startActivity(intent)")
            }
        }

        return parameterSpecs to codeBlockBuilder.build()
    }

    /**
     * 获取intent put方法.
     * @param paramName String
     * @return String
     */
    private fun getPutMethod(paramName: String): String {
        return "intent.putExtra(\"$paramName\", $paramName)"
    }

    /**
     * 获取文档代码块.
     * @param actName String
     * @return CodeBlock
     */
    private fun getKDocCodeBlock(actName: String) = CodeBlock.builder()
            .add("""
            启动$actName.

            """.trimIndent()).build()

}