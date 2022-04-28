package com.bihe0832.android.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

class InjectTransform extends Transform {

    private Project mProject

    InjectTransform(Project project) {
        this.mProject = project
    }

    @Override
    String getName() {
        return "TransformTestOfZixie"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transform的作用范围
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    // 当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }


    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        System.out.println("-------------------- transform -------------------")

        inputs.each {
            TransformInput input ->
                // 遍历文件夹
                input.directoryInputs.each {
                    DirectoryInput directoryInput ->
                        // 注入代码
                        MyInjectByJavassit.injectTest(directoryInput.file.absolutePath, mProject)
                        // 获取输出目录
                        def dest = outputProvider.getContentLocation(directoryInput.name,
                                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                        System.out.println("directory output dest: $dest.absolutePath")
                        FileUtils.copyDirectory(directoryInput.file, dest)
                }

                //遍历 jar文件
                input.jarInputs.each {
                    JarInput jarInput ->
                        // 重命名输出文件（同目录copyFile会冲突）
                        def jarName = jarInput.name
                        System.out.println("jar: $jarInput.file.absolutePath")
                        def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                        if (jarName.endsWith('.jar')) {
                            jarName = jarName.substring(0, jarName.length() - 4)
                        }
                        def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                        System.out.println("jar output dest: $dest.absolutePath")
                        FileUtils.copyFile(jarInput.file, dest)
                }
        }

        println '---------------------transform-------------------'
    }
}