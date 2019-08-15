package com.zp.v1.controller;

import com.zp.annoction.MyAutowired;
import com.zp.annoction.MyController;
import com.zp.annoction.MyRequestMapping;
import com.zp.v1.service.IUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @创建人 pc801
 * @创建时间 2019/8/14
 * @描述
 */
@MyController
@MyRequestMapping("/user")
public class UserController {

    @MyAutowired
    private IUserService userService;

    @MyRequestMapping("/sayHello")
    public void sayHello(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("I'm ZP" + userService.sayHello());
    }

}
