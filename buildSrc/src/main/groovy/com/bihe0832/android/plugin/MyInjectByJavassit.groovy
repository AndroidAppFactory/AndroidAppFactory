package com.bihe0832.android.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

/**
 * 借助 Javassit 操作 Class 文件
 */
class MyInjectByJavassit {

    private static final ClassPool sClassPool = ClassPool.getDefault()

    static void injectTest(String path, Project project) {
        // 加入当前路径
        sClassPool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        sClassPool.importPackage('android.os.Bundle')

        File dir = new File(path)
        if (dir.isDirectory()) {
            // 遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("filePath: $filePath")

                if (file.name == 'TestMainActivity.class') {
                    // 获取Class
                    // 这里的MainActivity就在app模块里
                    CtClass ctClass = sClassPool.getCtClass('com.bihe0832.android.test.TestMainActivity')
                    println("ctClass: $ctClass")

                    // 解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    // 获取Method
                    CtMethod ctMethod = ctClass.getDeclaredMethod('onCreate')
                    println("ctMethod: $ctMethod")

                    String toastStr = """ android.util.Log.d("Zixie","这是一条来自Transform的日志");  
                                      """

                    // 方法尾插入
                    ctMethod.insertAfter(toastStr)
                    ctClass.writeFile(path)
                    ctClass.detach() //释放
                }
            }
        }
    }

}