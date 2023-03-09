package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @Author:zyc
 * @Date:2023-02-20-14:34
 * @Deacription:
 */
public interface SetmealService extends IService<Setmeal> {
    //新增套餐，同时需要保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    //修改套餐根据页面传回的套餐id 对套餐信息进行回显
    public SetmealDto getByIdWithDish(Long id);

    //更新套餐信息，同时更新对应的菜品信息
    public void updateWithDish(SetmealDto setmealDto);
    //删除套餐
    public void removeWithDish(List<Long> ids);
    //停售/启售套餐（单个/批量）
    public void status(Integer status, List<Long> ids);


}
