package com.zp.annoction;

import java.lang.annotation.*;

/**
 * @创建人 zp
 * @创建时间 2019/8/14
 * @描述 Controller注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    /**
     * 表示给controller注册别名
     * @return
     */
    String value() default "";
}
