package com.example

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException


/**
 * Created by Shufeng.Wu on 2017/8/16.
 */
@AutoService(Processor::class)
class FactoryProcessor : AbstractProcessor() {

    private lateinit var messager: Messager
    private var map: MutableMap<String, TypeElement> =
            LinkedHashMap()


    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        map.clear()
        for (annotatedElement in roundEnv.getElementsAnnotatedWith(Factory::class.java)) {
            if (annotatedElement.kind !== ElementKind.CLASS) {
                return true // Exit processing
            }

            val typeElement: TypeElement = annotatedElement as TypeElement
            val factory = typeElement.getAnnotation(Factory::class.java)
            try {
                //error(annotatedElement, factory.id() + " " + typeElement.getSimpleName().toString());
                //error(annotatedElement, "label1");
                map.put(factory.id, typeElement)

            } catch (mte: MirroredTypeException) {
            }

        }
        generateCode(map)
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Factory::class.java.canonicalName)
    }

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
    }

    fun generateCode(map: Map<String, TypeElement>) {
        val createBuilder = FunSpec.builder("create")
                .addParameter("id", String::class.java).beginControlFlow("if (null == id) ")
                .addStatement("throw IllegalArgumentException(\"name of meal is null!\")")
                .endControlFlow()

        for (key in map.keys) {
            createBuilder
                    .beginControlFlow("if (%S.equals(id)) ", key)
                    .addStatement("return %T()", ClassName.bestGuess(map[key]?.qualifiedName.toString()))
                    .endControlFlow()
        }


        createBuilder.addStatement("throw IllegalArgumentException(\"Unknown meal '\" + id + \"'\")")
                .addModifiers(KModifier.PUBLIC)
                .returns(ClassName.invoke("com.delta.test.kapttest", "Meal"))


        val create = createBuilder.build()

        val mealFactory = TypeSpec.classBuilder("MealFactory")
                .addModifiers(KModifier.PUBLIC)
                .addFun(create)
                .build()


        val kaptGeneratedDirPath = processingEnv.options["kapt.kotlin.generated"]
                ?.replace("kaptKotlin", "kapt") ?: run {
            error("can't generate kotlin file")
            return
        }

        val kaptGeneratedDir = File(kaptGeneratedDirPath)
        if (!kaptGeneratedDir.parentFile.exists()) {
            kaptGeneratedDir.parentFile.mkdirs()
        }

        try {
            KotlinFile.builder("com.delta.test.kapttest", "MealFactory")
                    .addType(mealFactory)
                    .build()
                    .writeTo(kaptGeneratedDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /*private val map = LinkedHashMap()
    private var processingEnv: ProcessingEnvironment? = null
    private var messager: Messager? = null

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        this.processingEnv = processingEnv
        messager = processingEnv.messager


    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

        map.clear()
        for (annotatedElement in roundEnvironment
                .getElementsAnnotatedWith(Factory::class)) {
            if (annotatedElement.getKind() !== ElementKind.CLASS) {
                return true // Exit processing
            }

            // We can cast it, because we know that it of ElementKind.CLASS
            val typeElement = annotatedElement
            val factory = typeElement.getAnnotation(Factory::class.java)
            try {
                //error(annotatedElement, factory.id() + " " + typeElement.getSimpleName().toString());
                //error(annotatedElement, "label1");
                map.put(factory.id, typeElement)
                messager!!.printMessage(
                        Diagnostic.Kind.NOTE,
                        "label2")
            } catch (mte: MirroredTypeException) {
            }

        }

        generateCode(map, processingEnv)
        return false
    }

    fun generateCode(map: Map<String, TypeElement>, processingEnv: ProcessingEnvironment) {
        val createBuilder = MethodSpec.methodBuilder("create").addParameter(String::class.java, "id").beginControlFlow("if (null == id) ")
                .addStatement("throw new IllegalArgumentException(\"name of meal is null!\")")
                .endControlFlow()

        for (key in map.keys) {
            createBuilder
                    .beginControlFlow("if (\$S.equals(id)) ", key)
                    .addStatement("return new \$T()", ClassName.get(map[key]))
                    .endControlFlow()
        }


        createBuilder.addStatement("throw new IllegalArgumentException(\"Unknown meal '\" + id + \"'\")")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("com.delta.test.aptlearning", "Meal"))


        val create = createBuilder.build()

        val mealFactory = TypeSpec.classBuilder("MealFactory")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(create)
                .build()

        val javaFile = JavaFile.builder("com.delta.test.aptlearning", mealFactory)
                .build()

        try {
            javaFile.writeTo(processingEnv.filer)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }*/
}