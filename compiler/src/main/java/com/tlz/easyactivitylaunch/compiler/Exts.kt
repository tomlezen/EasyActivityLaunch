package com.tlz.easyactivitylaunch.compiler

import com.squareup.kotlinpoet.*
import org.checkerframework.checker.units.qual.A
import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.lang.model.util.Elements
import javax.lang.model.util.SimpleTypeVisitor6
import javax.lang.model.util.SimpleTypeVisitor7
import javax.tools.Diagnostic
import kotlin.reflect.full.primaryConstructor


/**
 * Created by Tomlezen.
 * Data: 2018/8/27.
 * Time: 10:55.
 */

fun String.firstUpperCase(): String =
        if (isEmpty() || length == 1) {
            this.toUpperCase()
        } else {
            substring(0, 1).toUpperCase() + substring(1)
        }

fun TypeElement.pkg(elementUtils: Elements) = elementUtils.getPackageOf(this).qualifiedName.toString()

fun TypeName.isFloat() = this == FLOAT
fun TypeName.isString() = this == String::class.asTypeName()

fun TypeMirror.isSubtypeOfType(otherType: String): Boolean {
    if (isTypeEqual(this, otherType)) {
        return true
    }
    if (kind != TypeKind.DECLARED) {
        return false
    }
    val declaredType = this as DeclaredType
    val typeArguments = declaredType.typeArguments
    if (typeArguments.size > 0) {
        val typeString = StringBuilder(declaredType.asElement().toString())
        typeString.append('<')
        for (i in typeArguments.indices) {
            if (i > 0) {
                typeString.append(',')
            }
            typeString.append('?')
        }
        typeString.append('>')
        if (typeString.toString() == otherType) {
            return true
        }
    }
    val element = declaredType.asElement() as? TypeElement ?: return false
    val superType = element.superclass
    if (superType.isSubtypeOfType(otherType)) {
        return true
    }
    for (interfaceType in element.interfaces) {
        if (interfaceType.isSubtypeOfType(otherType)) {
            return true
        }
    }
    return false
}

private fun isTypeEqual(typeMirror: TypeMirror, otherType: String): Boolean =
        otherType == typeMirror.toString()

fun TypeMirror.typeName(): TypeName {
    val name = toString()
    return when (name) {
        "java.lang.Object" -> Any::class.asTypeName()
        "java.lang.String" -> String::class.asTypeName()
        "boolean", "java.lang.Boolean" -> Boolean::class.asTypeName()
        "byte", "java.lang.Byte" -> Byte::class.asTypeName()
        "char", "java.lang.Character" -> Char::class.asTypeName()
        "short", "java.lang.Short" -> Short::class.asTypeName()
        "int", "java.lang.Integer" -> Int::class.asTypeName()
        "long", "java.lang.Long" -> Long::class.asTypeName()
        "float", "java.lang.Float" -> Float::class.asTypeName()
        "double", "java.lang.Double" -> Double::class.asTypeName()
        "java.lang.CharSequence" -> CharSequence::class.asTypeName()
        "java.lang.String[]" -> Array<String>::class.asTypeName()
        else -> {
            if (name.endsWith("[]")) {
                accept(object : SimpleTypeVisitor7<TypeName, Void?>() {
                    override fun visitArray(p0: ArrayType?, p1: Void?): TypeName {
                        return ARRAY
                    }
                }, null)
            } else {
                return asTypeName()
            }
        }
    }
}

fun ClassName.check() =
        when (canonicalName) {
            "java.lang.Object" -> Any::class.asTypeName()
            "java.lang.String" -> String::class.asTypeName()
            "boolean", "java.lang.Boolean" -> Boolean::class.asTypeName()
            "byte", "java.lang.Byte" -> Byte::class.asTypeName()
            "char", "java.lang.Character" -> Char::class.asTypeName()
            "short", "java.lang.Short" -> Short::class.asTypeName()
            "int", "java.lang.Integer" -> Int::class.asTypeName()
            "long", "java.lang.Long" -> Long::class.asTypeName()
            "float", "java.lang.Float" -> Float::class.asTypeName()
            "double", "java.lang.Double" -> Double::class.asTypeName()
            "java.lang.CharSequence" -> CharSequence::class.asTypeName()
            else -> this
        }

//fun TypeMirror.asCustomTypeName(): TypeName {
//    return this.accept(object : SimpleTypeVisitor7<TypeName, Void?>() {
//        override fun visitPrimitive(t: PrimitiveType, p: Void?) =
//                when (t.kind) {
//                    TypeKind.BOOLEAN -> BOOLEAN
//                    TypeKind.BYTE -> BYTE
//                    TypeKind.SHORT -> SHORT
//                    TypeKind.INT -> INT
//                    TypeKind.LONG -> LONG
//                    TypeKind.CHAR -> CHAR
//                    TypeKind.FLOAT -> FLOAT
//                    TypeKind.DOUBLE -> DOUBLE
//                    else -> throw AssertionError()
//                }
//
//        override fun visitDeclared(t: DeclaredType, p: Void?): TypeName {
//            val rawType: ClassName = (t.asElement() as TypeElement).asCustomClassName()
//            val enclosingType = t.enclosingType
//            val enclosing = if (enclosingType.kind != TypeKind.NONE && !t.asElement().modifiers.contains(
//                            Modifier.STATIC)) enclosingType.accept(this, null) else null
//            if (t.typeArguments.isEmpty() && enclosing !is ParameterizedTypeName) {
//                return rawType.check()
//            }
//
//            val typeArgumentNames = mutableListOf<TypeName>()
//            for (typeArgument in t.typeArguments) {
//                typeArgumentNames += typeArgument.asCustomTypeName()
//            }
//            val constructor = ParameterizedTypeName::class.primaryConstructor
//            return (enclosing as? ParameterizedTypeName)?.nestedClass(rawType.simpleName,
//                    typeArgumentNames) ?: constructor?.call(null, rawType, typeArgumentNames, false,
//                    listOf<AnnotationSpec>())!!
//        }
//
//        override fun visitError(t: ErrorType, p: Void?): TypeName = visitDeclared(t, p)
//
//        override fun visitArray(t: ArrayType, p: Void?):  =
//                ParameterizedTypeName(get(type.componentType, map))
//
//        override fun visitTypeVariable(t: TypeVariable, p: Void?): TypeName = t.asCustomTypeName()
//
//        override fun visitWildcard(t: WildcardType, p: Void?): TypeName = t.asWildcardTypeName()
//
//        override fun visitNoType(t: NoType, p: Void?): TypeName {
//            if (t.kind == TypeKind.VOID) return UNIT
//            return super.visitUnknown(t, p)
//        }
//
//        override fun defaultAction(e: TypeMirror?, p: Void?): TypeName {
//            throw IllegalArgumentException("Unexpected type mirror: " + e!!)
//        }
//    }, null)
//}

private fun isClassOrInterface(e: Element): Boolean = e.kind.isClass || e.kind.isInterface

private fun getPackage(type: Element): PackageElement {
    var t = type
    while (t.kind != ElementKind.PACKAGE) {
        t = t.enclosingElement
    }
    return t as PackageElement
}

fun TypeElement.asCustomClassName(): ClassName {
    val names = mutableListOf<String>()
    var e: Element = this
    while (isClassOrInterface(e)) {
        val eType = e as TypeElement
        require(eType.nestingKind == NestingKind.TOP_LEVEL || eType.nestingKind == NestingKind.MEMBER) {
            "unexpected type testing"
        }
        names += eType.simpleName.toString()
        e = eType.enclosingElement
    }
    val name = getPackage(this).qualifiedName.toString()
    names += if (name.startsWith("java.util")) "kotlin.collections" else name
    names.reverse()
    return ClassName::class.primaryConstructor?.call(names, false, listOf<AnnotationSpec>())!!
}

fun TypeElement.getAnnotationMirror(clazz: Class<*>): AnnotationMirror? {
    val clazzName = clazz.name
    for (m in annotationMirrors) {
        if (m.annotationType.toString() == clazzName) {
            return m
        }
    }
    return null
}

fun AnnotationMirror.getAnnotationValue(key: String): AnnotationValue? {
    for ((key1, value) in elementValues) {
        if (key1.simpleName.toString() == key) {
            return value
        }
    }
    return null
}
