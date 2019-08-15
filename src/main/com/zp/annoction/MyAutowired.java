package com.zp.annoction;

import java.lang.annotation.*;

/**
 * @创建人 zp
 * @创建时间 2019/8/14
 * @描述 Autowired注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {

    String value() default "";

}
