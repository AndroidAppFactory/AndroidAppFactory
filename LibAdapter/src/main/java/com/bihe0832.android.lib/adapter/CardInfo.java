package com.bihe0832.android.lib.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)//次注解作用于类和字段上
public @interface CardInfo {
    int id() default 0;
    Class hoderCalss() default Object.class;

}
