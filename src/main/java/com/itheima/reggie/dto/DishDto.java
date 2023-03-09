package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 注释Date 自创实体，用户新增菜品时，页面传输的数据 不是与一个实体属性一一对应。
 * @Author:zyc
 * @Date:2023-02-21-19:14
 * @Deacription:
 */
@Data
public class DishDto extends Dish {
    //口味信息
    private List<DishFlavor> flavors= new ArrayList<>();

    private  String categoryName;



    private Integer copies;
}
