package com.zp.annoction;

import java.lang.annotation.*;

/**
 * @创建人 zp
 * @创建时间 2019/8/14
 * @描述 RequestParam
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {

    String value();

}
