package com.zp.v1.service.impl;

import com.zp.annoction.MyService;
import com.zp.v1.service.IUserService;

/**
 * @创建人 zp
 * @创建时间 2019/8/14
 * @描述
 */
@MyService
public class UserServiceImpl implements IUserService {


    @Override
    public String sayHello() {
        return " HelloWord!!";
    }
}
