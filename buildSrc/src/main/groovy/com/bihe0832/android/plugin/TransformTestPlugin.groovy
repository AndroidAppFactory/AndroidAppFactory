package com.bihe0832.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * TransformTestPlugin
 */
class TransformTestPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        // 获取扩展
        def android = project.extensions.getByType(AppExtension)
        // 注册Transform
        android.registerTransform(new InjectTransform(project))

    }
}