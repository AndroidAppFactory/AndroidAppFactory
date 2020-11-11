package com.bihe0832.android.plugin

import com.bihe0832.android.lib.router.annotation.Module
import javassist.*
import org.gradle.api.Project

/**
 * 借助 Javassit 操作 Class 文件
 */
class MyInjectByJavassit {

    private static final ClassPool sClassPool = ClassPool.getDefault()

    static void injectTest(String path, Project project) {
//        testNewMethod(path, project)
    }

    static void testNewMethod(String path, Project project) {
        // 加入当前路径
        sClassPool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        sClassPool.importPackage('android.os.Bundle')
        sClassPool.importPackage('android.content.Context')
        sClassPool.importPackage('android.view.View')

        File dir = new File(path)
        if (dir.isDirectory()) {
            // 遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("filePath: $filePath")

                if (file.name == 'TestMainFragment.class') {

                    CtClass ctClass = sClassPool.getCtClass('com.bihe0832.android.test.TestMainFragment')
                    println("ctClass: $ctClass")

                    // 解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    CtClass realCalss = sClassPool.getCtClass('com.bihe0832.android.test.base.TestItem')
                    sClassPool.insertClassPath(new ClassClassPath(realCalss.class))

                    // 添加方法
                    CtMethod say = new CtMethod(realCalss, "testZixie", null, ctClass)
                    say.setModifiers(Modifier.PUBLIC)
                    say.setBody("{" +
                            "System.out.print(\"这是一个子勰测试：" + realCalss.name + "\");\n" +
                            "        return null;\n\n" +
                            "}")
                    ctClass.addMethod(say)

                    CtMethod newmethod = CtNewMethod.make("public " + realCalss.name + " testZixie1(String title,com.bihe0832.android.test.base.OnTestItemClickListener listener){\n" +
                            "        return new com.bihe0832.android.test.base.TestItem(title,listener);\n\n" +
                            "    }", ctClass);
                    ctClass.addMethod(newmethod)

                    ctClass.writeFile(path)
                    ctClass.detach() //释放

                }
            }
        }
    }

    static void testSimpleTransform(String path, Project project) {
        // 加入当前路径
        sClassPool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        sClassPool.importPackage('android.os.Bundle')
        sClassPool.importPackage('android.content.Context')
        sClassPool.importPackage('android.view.View')

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
                    String toastStr = "android.util.Log.d(\"Zixie\",\"这是一条来自Transform的日志\"); "

                    //方法尾插入
                    ctMethod.insertAfter(toastStr)
                    ctClass.writeFile(path)
                    ctClass.detach() //释放
                }
            }
        }
    }

    static void testAnnotationTransform(String path, Project project) {
        // 加入当前路径
        sClassPool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        sClassPool.importPackage('android.os.Bundle')
        sClassPool.importPackage('android.content.Context')
        sClassPool.importPackage('android.view.View')

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

                    Object[] clazzAnnotations = ctClass.getAnnotations();
                    Module moduleValue = (Module) clazzAnnotations[0];
                    System.out.println("Zixie" + moduleValue.value());

                    String toastStr = "android.util.Log.d(\"Zixie\",\"这是一条来自Transform的日志" + moduleValue.value() + "\"); "

                    //方法尾插入
                    ctMethod.insertAfter(toastStr)
                    ctClass.writeFile(path)
                    ctClass.detach() //释放
                }

            }
        }
    }
}