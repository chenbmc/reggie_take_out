package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @Author:zyc
 * @Date:2023-02-20-11:27
 * @Deacription:
 */
public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
