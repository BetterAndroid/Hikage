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
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.highcapable.hikage.compiler.DeclaredSymbol
import com.highcapable.hikage.compiler.extension.ClassDetector
import com.highcapable.hikage.compiler.extension.asType
import com.highcapable.hikage.compiler.extension.getClassDeclaration
import com.highcapable.hikage.compiler.extension.getOrNull
import com.highcapable.hikage.compiler.extension.getSimpleNameString
import com.highcapable.hikage.compiler.extension.getTypedSimpleName
import com.highcapable.hikage.compiler.extension.isClass
import com.highcapable.hikage.compiler.extension.isSubclassOf
import com.highcapable.hikage.compiler.extension.ownerOf
import com.highcapable.hikage.compiler.subprocessor.base.BaseSymbolProcessor
import com.highcapable.hikage.generated.HikageProperties
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class HikageViewGenerator(override val environment: SymbolProcessorEnvironment) : BaseSymbolProcessor(environment) {

    private companion object {

        private const val VIEW_DECLARATION_FILES_OPTION = "hikage.viewDeclarationFiles"
        private const val VIEW_DECLARATION_FILE_NAME = "HikageViewDeclarationFile"

        private const val PACKAGE_NAME_PREFIX = "com.highcapable.hikage.widget"

        private const val VIEW_FUNCTION_ALIAS = "_View"
        private const val VIEW_GROUP_FUNCTION_ALIAS = "_ViewGroup"

        private const val VIEW_METADATA_CLASS_SUFFIX = "HikageView"
        private const val VIEW_METADATA_FUNCTION_NAME = "FUNCTION_NAME"

        val HikageableClass = ClassName(DeclaredSymbol.ANNOTATION_PACKAGE_NAME, DeclaredSymbol.HIKAGEABLE_ANNOTATION_CLASS_NAME)
        val LayoutParamClass = ClassName(DeclaredSymbol.HIKAGE_LAYOUT_PACKAGE_NAME, DeclaredSymbol.HIKAGE_LAYOUT_PARAMS_CLASS_NAME)
        val ViewGroupLpClass = ClassName(DeclaredSymbol.ANDROID_VIEW_PACKAGE_NAME, DeclaredSymbol.ANDROID_LAYOUT_PARAMS_CLASS_NAME)
        val PerformerClass = ClassName(DeclaredSymbol.HIKAGE_CORE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_CLASS_NAME, "Performer")
        val ViewLambdaClass = ClassName(DeclaredSymbol.HIKAGE_BASE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_VIEW_LAMBDA_CLASS_NAME)
        val PerformerLambdaClass = ClassName(DeclaredSymbol.HIKAGE_BASE_PACKAGE_NAME, DeclaredSymbol.HIKAGE_PERFORMER_LAMBDA_CLASS_NAME)
        val AttributeClass = ClassName(DeclaredSymbol.HIKAGE_ATTRS_PACKAGE_NAME, DeclaredSymbol.HIKAGE_ATTRIBUTE_LAMBDA_CLASS_NAME)
        val ViewFunction = MemberName(DeclaredSymbol.HIKAGE_LAYOUT_PACKAGE_NAME, "View")
        val ViewGroupFunction = MemberName(DeclaredSymbol.HIKAGE_LAYOUT_PACKAGE_NAME, "ViewGroup")

        val jsonDecoder = Json
    }

    private val generatedFileOwners = mutableMapOf<String, String>()
    private val annotationViewKeys = mutableSetOf<String>()

    override fun startProcess(resolver: Resolver) {
        Processor.init(logger, resolver)

        val annotationPerformers = mutableListOf<Performer>()
        resolver.getSymbolsWithAnnotation(HikageViewSpec.CLASS)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName }
            .forEach { ksClass ->
                ksClass.annotations.filter { it.isClass(HikageViewSpec.CLASS) }.forEach {
                    // Get annotation parameters.
                    val (annotation, declaration) = HikageViewSpec.create(resolver, it, ksClass)

                    annotationPerformers += Performer(annotation, declaration, ksClass.containingFile, ksClass.ownerOf(HikageViewSpec.NAME))
                }
            }

        resolver.getSymbolsWithAnnotation(HikageViewDeclarationSpec.CLASS)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName }
            .forEach { ksClass ->
                ksClass.annotations.filter { it.isClass(HikageViewDeclarationSpec.CLASS) }.forEach {
                    // Get annotation parameters.
                    val (annotation, declaration) = HikageViewDeclarationSpec.create(resolver, it, ksClass)

                    annotationPerformers += Performer(annotation, declaration, ksClass.containingFile, ksClass.ownerOf(HikageViewDeclarationSpec.NAME))
                }
            }

        annotationViewKeys += annotationPerformers.map { it.declaration.viewClassKey }
        val filePerformers = createViewDeclarationFilePerformers(resolver)
            .filter { it.declaration.viewClassKey !in annotationViewKeys }
        val performers = annotationPerformers + filePerformers

        processPerformer(performers, mutableSetOf())
    }

    private fun createViewDeclarationFilePerformers(resolver: Resolver): List<Performer> {
        val files = environment.options[VIEW_DECLARATION_FILES_OPTION]
            ?.split(File.pathSeparator)
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.distinct()
            ?.map(::File)
            ?: return emptyList()

        return files.flatMap { file ->
            require(file.exists() && file.isFile) {
                "Hikage view declaration file does not exist or is not a file: ${file.path}"
            }

            val items = try {
                jsonDecoder.decodeFromString<List<ViewDeclarationFileItem>>(file.readText())
            } catch (e: SerializationException) {
                error("Parse Hikage view declaration file failed: ${file.path}\n${e.message}")
            } catch (e: IOException) {
                error("Read Hikage view declaration file failed: ${file.path}\n${e.message}")
            }

            items.mapIndexed { index, item ->
                val (annotation, declaration) = ViewDeclarationFileSpec.create(resolver, item, file, index)
                Performer(annotation, declaration, null, "$VIEW_DECLARATION_FILE_NAME: ${file.path}[$index]")
            }
        }
    }

    private fun processPerformer(performers: List<Performer>, roundGeneratedFiles: MutableSet<String>) {
        val duplicatedItems = performers.groupBy { it.declaration.key }.filter { it.value.size > 1 }.flatMap { it.value }

        require(duplicatedItems.isEmpty()) {
            "Discover duplicate @HikageView or @HikageViewDeclaration's class name or alias definitions, " +
                "you can re-specify the class name using the `alias` parameter.\n" +
                "Duplicated Items:\n" +
                duplicatedItems.joinToString("\n") { "${it.declaration}\n${it.declaration.locateDesc}" }
        }
        performers.forEach { generateCodeFile(it, roundGeneratedFiles) }
    }

    private fun generateCodeFile(performer: Performer, roundGeneratedFiles: MutableSet<String>) {
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
        val metadataClassName = "_${performer.declaration.className.replace(".", "_")}$VIEW_METADATA_CLASS_SUFFIX"

        val hasPerformer = lparamsClass != null && !performer.annotation.final
        val performFunctionAlias = if (hasPerformer) VIEW_GROUP_FUNCTION_ALIAS else VIEW_FUNCTION_ALIAS

        val codeFileSpec = FileSpec.builder(packageName, fileName).apply {
            addCopyrightFileComment()

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
            addAliasedImport(if (hasPerformer) ViewGroupFunction else ViewFunction, performFunctionAlias)

            // Kotlin's import rule is to introduce the parent class that also needs to be introduced at the same time.
            // If a child class exists, it needs to import the parent class,
            // and kotlinpoet will not perform this operation automatically.
            viewClass.first?.let { addImport(it.packageName, it.simpleName) }
            lparamsClass?.first?.let { addImport(it.packageName, it.simpleName) }

            // May conflict with other [LayoutParams].
            lparamsClass?.second?.let { addAliasedImport(it, it.getTypedSimpleName()) }

            addAliasedImport(ViewGroupLpClass, ViewGroupLpClass.getTypedSimpleName())

            addFunction(FunSpec.builder(classNameSet).apply {
                addKdoc(
                    """
                      Resolve the [${performer.declaration.className}].
                      @see Hikage.Performer
                      @return [${performer.declaration.className}]
                    """.trimIndent()
                )

                addAnnotation(HikageableClass)
                addModifiers(KModifier.INLINE)
                addTypeVariable(TypeVariableName(name = "LP", ViewGroupLpClass).copy(reified = true))
                receiver(PerformerClass.parameterizedBy(TypeVariableName("LP")))

                addParameter(
                    ParameterSpec.builder(name = "lparams", LayoutParamClass.copy(nullable = true))
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
                        name = "attrs",
                        type = AttributeClass,
                        modifiers = listOf(KModifier.NOINLINE)
                    ).apply {
                        if (!performer.annotation.requireAttrs) defaultValue("{}")
                    }.build()
                )
                addParameter(
                    ParameterSpec.builder(
                        name = "init",
                        type = ViewLambdaClass.parameterizedBy(viewClass.second),
                        modifiers = listOf(KModifier.NOINLINE)
                    ).apply {
                        if (!performer.annotation.requireInit) defaultValue("{}")
                    }.build()
                )
                lparamsClass?.second?.takeIf { hasPerformer }?.let {
                    addParameter(
                        ParameterSpec.builder(
                            name = "performer",
                            type = PerformerLambdaClass.parameterizedBy(it),
                            modifiers = listOf(KModifier.NOINLINE)
                        ).apply {
                            if (!performer.annotation.requirePerformer) defaultValue("{}")
                        }.build()
                    )
                    addStatement(
                        "return $performFunctionAlias(${performer.declaration.className}::class, " +
                            "${it.simpleName}::class, lparams, id, attrs, init, performer)"
                    )
                } ?: addStatement("return $performFunctionAlias(${performer.declaration.className}::class, lparams, id, attrs, init)")

                returns(viewClass.second)
            }.build())
        }.build()

        val metadataFileSpec = FileSpec.builder(packageName, metadataClassName).apply {
            addCopyrightFileComment()
            addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .addMember("%S", "unused")
                    .build()
            )
            addType(
                TypeSpec.objectBuilder(metadataClassName).apply {
                    addKdoc("Metadata for Hikage generated widget function.")
                    addProperty(
                        PropertySpec.builder(VIEW_METADATA_FUNCTION_NAME, String::class.asTypeName(), KModifier.CONST)
                            .initializer("%S", classNameSet)
                            .build()
                    )
                }.build()
            )
        }.build()

        val dependencies = performer.sourceFile?.let {
            Dependencies(aggregating = false, it)
        } ?: Dependencies(aggregating = false)

        writeCodeFile(
            fileSpec = codeFileSpec,
            generatedKey = "$packageName.$fileName",
            performer = performer,
            roundGeneratedFiles = roundGeneratedFiles,
            dependencies = dependencies
        )
        writeCodeFile(
            fileSpec = metadataFileSpec,
            generatedKey = "$packageName.$metadataClassName",
            performer = performer,
            roundGeneratedFiles = roundGeneratedFiles,
            dependencies = dependencies
        )
    }

    private fun FileSpec.Builder.addCopyrightFileComment() = addFileComment(
        """
          Hikage - ${HikageProperties.PROJECT_DESCRIPTION}
          Copyright (C) 2019 HighCapable
          ${HikageProperties.PROJECT_URL}

          This file is auto generated by Hikage.
          **DO NOT EDIT THIS FILE MANUALLY**
        """.trimIndent()
    )

    private fun writeCodeFile(
        fileSpec: FileSpec,
        generatedKey: String,
        performer: Performer,
        roundGeneratedFiles: MutableSet<String>,
        dependencies: Dependencies
    ) {
        if (!roundGeneratedFiles.add(generatedKey)) error(
            "Generate Hikage performer \"$generatedKey\" failed, the output file already exists in this round. " +
                "Please check whether the class name or alias is duplicated.\n${performer.declaration.locateDesc}"
        )
        generatedFileOwners[generatedKey]?.let {
            if (it == performer.owner) return

            error(
                "Generate Hikage performer \"$generatedKey\" failed, the output file already exists in previous round. " +
                    "Please check whether the class name or alias is duplicated.\n" +
                    "Previous: $it\nCurrent: ${performer.owner}\n${performer.declaration.locateDesc}"
            )
        }
        generatedFileOwners[generatedKey] = performer.owner

        runCatching {
            fileSpec.writeTo(codeGenerator, dependencies)
        }.onFailure { 
            if (it is FileAlreadyExistsException) error(
                "Generate Hikage performer \"$generatedKey\" failed, the output file already exists. " +
                    "Please check whether the class name or alias is duplicated.\n${performer.declaration.locateDesc}"
            )
            else throw it
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
            baseType: KSClassDeclaration = ksClass,
            locateDesc: String = "Located: ${baseType.qualifiedName?.asString()}"
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

            val declaration = ViewDeclaration(packageName, className, _alias, isViewGroup, locateDesc)

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
        val locateDesc: String
    ) {

        val key get() = "$packageName${alias ?: className}$isViewGroup"
        val viewClassKey get() = "$packageName.$className"

        fun toClassName() = ClassName(packageName, className)

        override fun toString() = "{ package: $packageName, class: $className, alias: ${alias ?: "<unspec>"} }"
    }

    private data class HikageViewSpec(
        override val lparams: KSClassDeclaration?,
        override val alias: String?,
        override val requireAttrs: Boolean,
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
                val requireAttrs = annotation.arguments.getOrNull<Boolean>("requireAttrs") ?: false
                val requireInit = annotation.arguments.getOrNull<Boolean>("requireInit") ?: false
                val requirePerformer = annotation.arguments.getOrNull<Boolean>("requirePerformer") ?: false
                val final = annotation.arguments.getOrNull<Boolean>("final") ?: false

                // Solve the actual content of the annotation parameters.
                val declaration = Processor.createViewDeclaration(NAME, alias, ksClass)
                val resolvedLparams = Processor.resolvedLparamsDeclaration(NAME, resolver, declaration, lparams)

                return HikageViewSpec(resolvedLparams, alias, requireAttrs, requireInit, requirePerformer, final) to declaration
            }
        }
    }

    private data class HikageViewDeclarationSpec(
        val view: KSClassDeclaration?,
        override val lparams: KSClassDeclaration?,
        override val alias: String?,
        override val requireAttrs: Boolean,
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
                val requireAttrs = annotation.arguments.getOrNull<Boolean>("requireAttrs") ?: false
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
                return HikageViewDeclarationSpec(resolvedView, resolvedLparams, alias, requireAttrs, requireInit, requirePerformer, final) to declaration
            }
        }
    }

    private data class ViewDeclarationFileSpec(
        override val lparams: KSClassDeclaration?,
        override val alias: String?,
        override val requireAttrs: Boolean,
        override val requireInit: Boolean,
        override val requirePerformer: Boolean,
        override val final: Boolean
    ) : HikageAnnotationSpec {

        companion object {

            fun create(
                resolver: Resolver,
                item: ViewDeclarationFileItem,
                file: File,
                index: Int
            ): Pair<ViewDeclarationFileSpec, ViewDeclaration> {
                val viewClass = item.viewClass.trim()
                require(viewClass.isNotEmpty()) {
                    "Declares $VIEW_DECLARATION_FILE_NAME's viewClass must not be empty.\nLocated: ${file.path}[$index]"
                }

                val resolvedView = resolver.getClassDeclarationByName(viewClass)
                    ?: error("Declares $VIEW_DECLARATION_FILE_NAME's viewClass \"$viewClass\" was not found.\nLocated: ${file.path}[$index]")
                val lparams = item.lparams?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    resolver.getClassDeclarationByName(it)
                        ?: error("Declares $VIEW_DECLARATION_FILE_NAME's lparams \"$it\" was not found.\nLocated: ${file.path}[$index]")
                }

                val declaration = Processor.createViewDeclaration(
                    tagName = VIEW_DECLARATION_FILE_NAME,
                    alias = item.alias,
                    ksClass = resolvedView,
                    locateDesc = "Located: ${file.path}[$index]"
                )
                val resolvedLparams = Processor.resolvedLparamsDeclaration(
                    tagName = VIEW_DECLARATION_FILE_NAME,
                    resolver = resolver,
                    declaration = declaration,
                    lparams = lparams?.asType()
                )
                val spec = ViewDeclarationFileSpec(
                    lparams = resolvedLparams,
                    alias = item.alias,
                    requireAttrs = item.requireAttrs,
                    requireInit = item.requireInit,
                    requirePerformer = item.requirePerformer,
                    final = item.final
                )

                return spec to declaration
            }
        }
    }

    @Serializable
    private data class ViewDeclarationFileItem(
        val viewClass: String,
        val lparams: String? = null,
        val alias: String? = null,
        val requireAttrs: Boolean = false,
        val requireInit: Boolean = false,
        val requirePerformer: Boolean = false,
        val final: Boolean = false
    )

    private interface HikageAnnotationSpec {
        val lparams: KSClassDeclaration?
        val alias: String?
        val requireAttrs: Boolean
        val requireInit: Boolean
        val requirePerformer: Boolean
        val final: Boolean
    }

    private data class Performer(
        val annotation: HikageAnnotationSpec,
        val declaration: ViewDeclaration,
        val sourceFile: KSFile?,
        val owner: String
    )
}