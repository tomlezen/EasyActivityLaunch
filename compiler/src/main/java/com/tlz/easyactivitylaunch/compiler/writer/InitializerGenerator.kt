package com.tlz.easyactivitylaunch.compiler.writer

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
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
import javax.lang.model.type.TypeMirror
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
      functions.addAll(createLaunchFunctions(clazz, element))
    }
    return functions
  }

  /**
   * 创建启动方法.
   * @param clazz Class<out Any>
   * @param element Element
   * @return List<FunSpec>
   */
  private fun createLaunchFunctions(clazz: Class<out Any>, element: Element): List<FunSpec> {
    val typeElement = element as TypeElement
    val (nickName, fragmentSupport) = Pair(
        typeElement.getAnnotationMirror(clazz)?.getAnnotationValue("nickName")?.value as? String
            ?: "",
        typeElement.getAnnotationMirror(clazz)?.getAnnotationValue("fragmentSupport")?.value as? Boolean
            ?: false
    )
    val actName = if (nickName.isEmpty()) element.asType().toString().split(".").last() else nickName.firstUpperCase()
    val actFuncBuilder = FunSpec.builder(if (clazz == EasyLaunchForResult::class.java) "launch${actName}ForResult" else "launch$actName").receiver(Activity::class)
    val frgFuncBuilder = if (fragmentSupport) FunSpec.builder("launch$actName").receiver(Fragment::class) else null
    getKDocCodeBlock(actName).apply {
      actFuncBuilder.addKdoc(this)
      frgFuncBuilder?.addKdoc(this)
    }
    createParametersAndBody(clazz, typeElement).apply {
      actFuncBuilder.addParameters(first)
      actFuncBuilder.addCode(second)
    }
    return mutableListOf(actFuncBuilder.build()).apply {
      frgFuncBuilder?.let {
        createParametersAndBody(clazz, element, true).apply {
          it.addParameters(first)
          it.addCode(second)
        }
        add(it.build())
      }
    }
  }

  /**
   * 获取参数列表.
   * @receiver TypeElement
   * @param clazz Class<out Any>
   * @return List<TypeMirror>
   */
  private fun TypeElement.getParameters(clazz: Class<out Any>): List<TypeMirror> =
      (getAnnotationMirror(clazz)?.getAnnotationValue("parameters") as? Attribute.Array)?.values?.mapTo(mutableListOf()) { (it as Attribute.Class).classType }
          ?: listOf()

  /**
   * 获取参数名列表.
   * @receiver TypeElement
   * @param clazz Class<out Any>
   * @return List<String>
   */
  private fun TypeElement.getParameterNames(clazz: Class<out Any>): List<String> =
      (getAnnotationMirror(clazz)?.getAnnotationValue("parameterNames") as? Attribute.Array)?.values?.mapTo(mutableListOf()) {
        it.toString().run {
          substring(1, length - 1)
        }
      } ?: listOf()

  /**
   * 获取标记列表.
   * @receiver TypeElement
   * @param clazz Class<out Any>
   * @return List<Int>
   */
  private fun TypeElement.getFlags(clazz: Class<out Any>): List<Int> =
      (getAnnotationMirror(clazz)?.getAnnotationValue("flags") as? Attribute.Array)?.values?.mapTo(mutableListOf()) {
        it.value as Int
      } ?: listOf()

  /**
   * 生成参数，和方法块.
   * @param element TypeElement
   * @return List<ParameterSpec>
   */
  private fun createParametersAndBody(clazz: Class<out Any>, element: TypeElement, isFragment: Boolean = false): Pair<List<ParameterSpec>, CodeBlock> {
    val parameterSpecs = mutableListOf<ParameterSpec>()
    val codeBlockBuilder = CodeBlock.builder()
    if (!isFragment) {
      codeBlockBuilder.addStatement("val intent = %T(this, %T::class.java)", Intent::class, element)
    } else {
      codeBlockBuilder.addStatement("val intent = %T(this.activity, %T::class.java)", Intent::class, element)
    }
    if (clazz == EasyLaunchForResult::class.java) {
      parameterSpecs.add(ParameterSpec.builder("requestCode", Int::class).build())
    }
    val (parameters, parameterNames, flags) = Triple(
        element.getParameters(clazz),
        element.getParameterNames(clazz),
        element.getFlags(clazz)
    )
    flags.forEach {
      codeBlockBuilder.addStatement("intent.addFlags($it)")
    }
    if (parameterNames.isEmpty() && parameters.isNotEmpty()) {
      parameters.mapIndexedTo(parameterSpecs) { index, kClass ->
        val paramName = "param${index + 1}"
        codeBlockBuilder.addStatement(getPutMethod(kClass, paramName))
        ParameterSpec.builder(paramName, kClass.typeName()).build()
      }
    } else if (parameterNames.isNotEmpty() && parameters.size != parameterNames.size) {
      messager.printMessage(Diagnostic.Kind.ERROR, "parameter size is ${parameters.size}, but parameterName size is ${parameterNames.size}")
    } else {
      parameters.mapIndexedTo(parameterSpecs) { index, kClass ->
        val paramName = parameterNames[index]
        codeBlockBuilder.addStatement(getPutMethod(kClass, paramName))
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
   * @param type TypeMirror
   * @param paramName String
   * @return String
   */
  private fun getPutMethod(type: TypeMirror, paramName: String): String =
      when (type.toString()) {
        "android.content.Intent", "android.os.Bundle" ->
          "intent.putExtras($paramName)"
        else -> "intent.putExtra(\"$paramName\", $paramName)"
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