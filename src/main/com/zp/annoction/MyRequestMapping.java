package com.zp.annoction;

import java.lang.annotation.*;

/**
 * @创建人 zp
 * @创建时间 2019/8/14
 * @描述 RequestMapping注解
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    String value() default "";

}
