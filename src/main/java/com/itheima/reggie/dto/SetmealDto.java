package com.itheima.reggie.dto;

import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:zyc
 * @Date:2023-02-23-10:50
 * @Deacription:
 */
@Data
public class SetmealDto extends Setmeal {
    private  String categoryName;
    private List<SetmealDish> setmealDishes = new ArrayList<>();

}
