/*
 * Hikage - An Android responsive UI building tool.
 * Copyright (C) 2019 HighCapable
 * https://github.com/BetterAndroid/Hikage
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2025/3/23.
 */
@file:Suppress("LocalVariableName")

package com.highcapable.hikage.compiler.subprocessor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.highcapable.hikage.compiler.DeclaredSymbol
import com.highcapable.hikage.compiler.extension.ClassDetector
import com.highcapable.hikage.compiler.extension.asType
import com.highcapable.hikage.compiler.extension.getClassDeclaration
import com.highcapable.hikage.compiler.extension.getOrNull
import com.highcapable.hikage.compiler.extension.getSimpleNameString
import com.highcapable.hikage.compiler.extension.getTypedSimpleName
import com.highcapable.hikage.compiler.extension.isSubclassOf
import com.highcapable.hikage.compiler.subprocessor.base.BaseSymbolProcessor
import com.highcapable.hikage.generated.HikageCompilerProperties
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class HikageViewGenerator(override val environment: SymbolProcessorEnvironment) : BaseSymbolProcessor(environment) {

    private companion object {

        private const val PACKAGE_NAME_PREFIX = "com.highcapable.hikage.widget"

        val HikageableClass = ClassName(DeclaredSymbol.ANNOTATION_PACKAGE_NAME, DeclaredSymbol.HIKAGEABLE_ANNOTATION_CLASS_NAME)
        val HikageLparamClass = ClassName(DeclaredSymbol.HIKAGE_CORE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_LAYOUT_PARAMS_CLASS_NAME)
        val ViewGroupLpClass = ClassName(DeclaredSymbol.ANDROID_VIEW_PACKAGE_NAME, DeclaredSymbol.ANDROID_LAYOUT_PARAMS_CLASS_NAME)
        val PerformerClass = ClassName(DeclaredSymbol.HIKAGE_CORE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_PERFORMER_CLASS_NAME)
        val ViewLambdaClass = ClassName(DeclaredSymbol.HIKAGE_BASE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_VIEW_LAMBDA_CLASS_NAME)
        val PerformerLambdaClass = ClassName(DeclaredSymbol.HIKAGE_BASE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_PERFORMER_LAMBDA_CLASS_NAME)
    }

    private val performers = mutableListOf<Performer>()

    override fun startProcess(resolver: Resolver) {
        Processor.init(logger, resolver)

        val dependencies = Dependencies(aggregating = true, *resolver.getAllFiles().toList().toTypedArray())
        resolver.getSymbolsWithAnnotation(HikageViewSpec.CLASS)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName }
            .forEach { ksClass ->
                ksClass.annotations.forEach {
                    // Get annotation parameters.
                    val (annotation, declaration) = HikageViewSpec.create(resolver, it, ksClass)

                    performers += Performer(annotation, declaration)
                }
            }

        resolver.getSymbolsWithAnnotation(HikageViewDeclarationSpec.CLASS)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName }
            .forEach { ksClass ->
                ksClass.annotations.forEach {
                    // Get annotation parameters.
                    val (annotation, declaration) = HikageViewDeclarationSpec.create(resolver, it, ksClass)

                    performers += Performer(annotation, declaration)
                }
            }

        processPerformer(dependencies)
    }

    private fun processPerformer(dependencies: Dependencies) {
        val duplicatedItems = performers.groupBy { it.declaration.key }.filter { it.value.size > 1 }.flatMap { it.value }

        require(duplicatedItems.isEmpty()) {
            "Discover duplicate @HikageView or @HikageViewDeclaration's class name or alias definitions, " +
                "you can re-specify the class name using the `alias` parameter.\n" +
                "Duplicated Items:\n" +
                duplicatedItems.joinToString("\n") { "${it.declaration}\n${it.declaration.locateDesc}" }
        }
        performers.forEach { generateCodeFile(dependencies, it) }
    }

    private fun generateCodeFile(dependencies: Dependencies, performer: Performer) {
        val classNameSet = performer.declaration.alias ?: performer.declaration.className
        val fileName = "_$classNameSet"

        val viewClass = performer.declaration.toClassName().let {
            val packageName = it.packageName
            val simpleName = it.simpleName
            val topClassName = if (simpleName.contains(".")) simpleName.split(".")[0] else null

            // com.example.MyViewScope
            // com.example.MyViewScope.MyView
            topClassName?.let { name -> ClassName(packageName, name) } to it
        }

        val lparamsClass = performer.annotation.lparams?.let {
            val packageName = it.packageName.asString()
            val subClassName = it.getSimpleNameString()
            val simpleName = it.simpleName.asString()
            val topClassName = subClassName.replace(".$simpleName", "")

            // android.view.ViewGroup
            // android.view.ViewGroup.LayoutParams
            (if (topClassName != subClassName)
                ClassName(packageName, topClassName)
            else null) to ClassName(packageName, subClassName)
        }

        val packageName = "$PACKAGE_NAME_PREFIX.${performer.declaration.packageName}"
        val fileSpec = FileSpec.builder(packageName, fileName).apply {
            addFileComment(
                """
                  Hikage - ${HikageCompilerProperties.POM_DESCRIPTION}
                  Copyright (C) 2019 HighCapable
                  ${HikageCompilerProperties.PROJECT_URL}

                  This file is auto generated by Hikage.
                  **DO NOT EDIT THIS FILE MANUALLY**
                """.trimIndent()
            )

            addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .addMember("%S, %S, %S", "unused", "FunctionName", "DEPRECATION")
                    .build()
            )
            addAnnotation(
                AnnotationSpec.builder(JvmName::class)
                    .addMember("%S", "${classNameSet}Performer")
                    .build()
            )

            addImport(DeclaredSymbol.ANDROID_VIEW_PACKAGE_NAME, DeclaredSymbol.ANDROID_VIEW_GROUP_CLASS_NAME)
            addImport(DeclaredSymbol.HIKAGE_CORE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_CLASS_NAME)

            // Kotlin's import rule is to introduce the parent class that also needs to be introduced at the same time.
            // If a child class exists, it needs to import the parent class,
            // and kotlinpoet will not perform this operation automatically.
            viewClass.first?.let { addImport(it.packageName, it.simpleName) }
            lparamsClass?.first?.let { addImport(it.packageName, it.simpleName) }

            // May conflict with other [LayoutParams].
            lparamsClass?.second?.let { addAliasedImport(it, it.getTypedSimpleName()) }

            addAliasedImport(ViewGroupLpClass, ViewGroupLpClass.getTypedSimpleName())
            addAliasedImport(HikageLparamClass, HikageLparamClass.getTypedSimpleName())

            addFunction(FunSpec.builder(classNameSet).apply {
                addKdoc(
                    """
                      Resolve the [${performer.declaration.className}].
                      @see ${performer.declaration.className}
                      @see Hikage.Performer.${if (lparamsClass == null) "View" else "ViewGroup"}
                      @return [${performer.declaration.className}]
                    """.trimIndent()
                )

                addAnnotation(HikageableClass)
                addModifiers(KModifier.INLINE)
                addTypeVariable(TypeVariableName(name = "LP", ViewGroupLpClass).copy(reified = true))
                receiver(PerformerClass.parameterizedBy(TypeVariableName("LP")))

                addParameter(
                    ParameterSpec.builder(name = "lparams", HikageLparamClass.copy(nullable = true))
                        .defaultValue("null")
                        .build()
                )
                addParameter(
                    ParameterSpec.builder(name = "id", String::class.asTypeName().copy(nullable = true))
                        .defaultValue("null")
                        .build()
                )
                addParameter(
                    ParameterSpec.builder(
                        name = "init",
                        ViewLambdaClass.parameterizedBy(viewClass.second)
                    ).apply { 
                        if (!performer.annotation.requireInit) defaultValue("{}")
                    }.build()
                )
                lparamsClass?.second?.takeIf { !performer.annotation.final }?.let {
                    addParameter(
                        ParameterSpec.builder(
                            name = "performer",
                            PerformerLambdaClass.parameterizedBy(it)
                        ).apply {
                            if (!performer.annotation.requirePerformer) defaultValue("{}")
                        }.build()
                    )
                    addStatement("return ViewGroup<${performer.declaration.className}, ${it.simpleName}>(lparams, id, init, performer)")
                } ?: addStatement("return View<${performer.declaration.className}>(lparams, id, init)")

                returns(viewClass.second)
            }.build())
        }.build()

        // May already exists, no need to generate.
        runCatching {
            fileSpec.writeTo(codeGenerator, dependencies)
        }.onFailure { 
            if (it !is FileAlreadyExistsException) throw it
        }
    }

    private object Processor {

        private lateinit var logger: KSPLogger

        private lateinit var viewDeclaration: KSClassDeclaration
        private lateinit var viewGroupDeclaration: KSClassDeclaration
        private lateinit var lparamsDeclaration: KSClassDeclaration

        fun init(logger: KSPLogger, resolver: Resolver) {
            this.logger = logger
            viewDeclaration = resolver.getClassDeclarationByName(DeclaredSymbol.ANDROID_VIEW_CLASS)!!
            viewGroupDeclaration = resolver.getClassDeclarationByName(DeclaredSymbol.ANDROID_VIEW_GROUP_CLASS)!!
            lparamsDeclaration = resolver.getClassDeclarationByName(DeclaredSymbol.ANDROID_LAYOUT_PARAMS_CLASS)!!
        }

        fun resolvedLparamsDeclaration(
            tagName: String,
            resolver: Resolver,
            declaration: ViewDeclaration,
            lparams: KSType?
        ): KSClassDeclaration? {
            val lparamsType = lparams?.takeIf { ksType ->
                ksType.declaration.qualifiedName?.asString() != Any::class.qualifiedName
            }
            var resolvedLparams = lparamsType?.declaration?.getClassDeclaration(resolver)

            when {
                // If the current view is not a view group but the lparams parameter is declared,
                // remove the lparams parameter.
                !declaration.isViewGroup && resolvedLparams != null -> {
                    logger.warn(
                        "Declares @$tagName's lparams \"${resolvedLparams.qualifiedName?.asString()}\" is invalid, " +
                            "because the current view is not a view group.\n${declaration.locateDesc}"
                    )
                    resolvedLparams = null
                }
                // If the current view is a view group but the lparams parameter is not declared,
                // set the default type parameter for it.
                declaration.isViewGroup && resolvedLparams == null -> resolvedLparams = lparamsDeclaration
            }

            // Verify layout parameter class.
            if (resolvedLparams != null) require(resolvedLparams.isSubclassOf(lparamsDeclaration.asType())) {
                val resolvedLparamsName = resolvedLparams.qualifiedName?.asString()
                "Declares @$tagName's lparams \"$resolvedLparamsName\" must be subclass of " +
                    "\"${DeclaredSymbol.ANDROID_LAYOUT_PARAMS_CLASS}\".\n${declaration.locateDesc}"
            }
            return resolvedLparams
        }

        fun createViewDeclaration(
            tagName: String,
            alias: String?,
            ksClass: KSClassDeclaration,
            baseType: KSClassDeclaration = ksClass
        ): ViewDeclaration {
            val packageName = ksClass.packageName.asString()
            val className = ksClass.getSimpleNameString()
            val isViewGroup = ksClass.isSubclassOf(viewGroupDeclaration.asType())

            var _alias = alias
            // If no alias name is set, if the class name contains a subclass,
            // replace it with an underscore and use it as an alias name.
            if (_alias.isNullOrBlank() && className.contains("."))
                _alias = className.replace(".", "_")
            _alias = _alias?.takeIf { it.isNotBlank() }

            val declaration = ViewDeclaration(packageName, className, _alias, isViewGroup, baseType)

            // Verify the legality of the class name.
            if (!_alias.isNullOrBlank()) require(ClassDetector.verify(_alias)) {
                "Declares @$tagName's alias \"$_alias\" is illegal.\n${declaration.locateDesc}"
            }

            // [ViewGroup] cannot be new instance.
            require(ksClass != viewGroupDeclaration) {
                "Declares @$tagName's class must not be a directly \"${DeclaredSymbol.ANDROID_VIEW_GROUP_CLASS}\".\n${declaration.locateDesc}"
            }
            // Annotations can only be modified on android view.
            require(ksClass.isSubclassOf(viewDeclaration.asType())) {
                "Declares @$tagName's class must be subclass of \"${DeclaredSymbol.ANDROID_VIEW_CLASS}\".\n${declaration.locateDesc}"
            }

            return declaration
        }
    }

    private data class ViewDeclaration(
        val packageName: String,
        val className: String,
        val alias: String?,
        val isViewGroup: Boolean,
        val baseType: KSClassDeclaration
    ) {

        val key get() = "$packageName${alias ?: className}$isViewGroup"

        val locateDesc by lazy { "Located: ${baseType.qualifiedName?.asString()}" }

        fun toClassName() = ClassName(packageName, className)

        override fun toString() = "{ package: $packageName, class: $className, alias: ${alias ?: "<unspec>"} }"
    }

    private data class HikageViewSpec(
        override val lparams: KSClassDeclaration?,
        override val alias: String?,
        override val requireInit: Boolean,
        override val requirePerformer: Boolean,
        override val final: Boolean
    ) : HikageAnnotationSpec {

        companion object {

            const val CLASS = DeclaredSymbol.HIKAGE_VIEW_ANNOTATION_CLASS
            const val NAME = DeclaredSymbol.HIKAGE_VIEW_ANNOTATION_CLASS_NAME

            fun create(
                resolver: Resolver,
                annotation: KSAnnotation,
                ksClass: KSClassDeclaration
            ): Pair<HikageViewSpec, ViewDeclaration> {
                val lparams = annotation.arguments.getOrNull<KSType>("lparams")
                val alias = annotation.arguments.getOrNull<String>("alias")
                val requireInit = annotation.arguments.getOrNull<Boolean>("requireInit") ?: false
                val requirePerformer = annotation.arguments.getOrNull<Boolean>("requirePerformer") ?: false
                val final = annotation.arguments.getOrNull<Boolean>("final") ?: false

                // Solve the actual content of the annotation parameters.
                val declaration = Processor.createViewDeclaration(NAME, alias, ksClass)
                val resolvedLparams = Processor.resolvedLparamsDeclaration(NAME, resolver, declaration, lparams)

                return HikageViewSpec(resolvedLparams, alias, requireInit, requirePerformer, final) to declaration
            }
        }
    }

    private data class HikageViewDeclarationSpec(
        val view: KSClassDeclaration?,
        override val lparams: KSClassDeclaration?,
        override val alias: String?,
        override val requireInit: Boolean,
        override val requirePerformer: Boolean,
        override val final: Boolean
    ) : HikageAnnotationSpec {

        companion object {

            const val CLASS = DeclaredSymbol.HIKAGE_HIKAGE_VIEW_DECLARATION_ANNOTATION_CLASS
            const val NAME = DeclaredSymbol.HIKAGE_HIKAGE_VIEW_DECLARATION_ANNOTATION_CLASS_NAME

            fun create(
                resolver: Resolver,
                annotation: KSAnnotation,
                ksClass: KSClassDeclaration
            ): Pair<HikageViewDeclarationSpec, ViewDeclaration> {
                val view = annotation.arguments.getOrNull<KSType>("view")
                val lparams = annotation.arguments.getOrNull<KSType>("lparams")
                val alias = annotation.arguments.getOrNull<String>("alias")
                val requireInit = annotation.arguments.getOrNull<Boolean>("requireInit") ?: false
                val requirePerformer = annotation.arguments.getOrNull<Boolean>("requirePerformer") ?: false
                val final = annotation.arguments.getOrNull<Boolean>("final") ?: false

                // Solve the actual content of the annotation parameters.
                val resolvedView = view?.declaration?.getClassDeclaration(resolver) ?: error("Internal error.")
                val declaration = Processor.createViewDeclaration(NAME, alias, resolvedView, ksClass)

                // Only object classes can be used as view declarations.
                require(ksClass.classKind == ClassKind.OBJECT) {
                    "Declares @$NAME's class must be an object.\n${declaration.locateDesc}"
                }
                require(!ksClass.isCompanionObject) {
                    "Declares @$NAME's class must not be a companion object.\n${declaration.locateDesc}"
                }

                val resolvedLparams = Processor.resolvedLparamsDeclaration(NAME, resolver, declaration, lparams)
                return HikageViewDeclarationSpec(resolvedView, resolvedLparams, alias, requireInit, requirePerformer, final) to declaration
            }
        }
    }

    private interface HikageAnnotationSpec {
        val lparams: KSClassDeclaration?
        val alias: String?
        val requireInit: Boolean
        val requirePerformer: Boolean
        val final: Boolean
    }

    private data class Performer(
        val annotation: HikageAnnotationSpec,
        val declaration: ViewDeclaration
    )
}