package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author:zyc
 * @Date:2023-02-20-14:33
 * @Deacription:
 */
public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
    //根据 id 查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);


    public  void status(Integer status, List<Long> ids);


    //根据 id 删除单个菜品或批量删除菜品
    public void deleteByIds(List<Long> ids);

}
