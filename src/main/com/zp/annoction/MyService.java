package com.zp.annoction;

import java.lang.annotation.*;

/**
 * @创建人 zp
 * @创建时间 2019/8/14
 * @描述 Service注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {

    String value() default "";

}
