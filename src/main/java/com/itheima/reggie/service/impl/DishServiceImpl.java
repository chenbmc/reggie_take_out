package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:zyc
 * @Date:2023-02-20-14:35
 * @Deacription:
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存味数据
     * @param dishDto
     * @Transactional 设计到多张表的操作
     */
    @Transactional
    public void saveWithFlavor(@RequestBody DishDto dishDto){
        //保存dish基本信息,以为在dishservice中 所以用this
        this.save(dishDto);

        //保存dish_flavor中的信息，所得信息为batch集合,但该集合中没有 dish_id 所以对集合进行处理
        Long dishId = dishDto.getId();

        //对集合数据进行处理 使每个item 中的dishId进行赋值
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜的口味 到dish_flavor表中 且对应dish_id
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    /**
     * 根据传的dish id 进行dish信息的回显
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //根据id获取dish的信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //查询对应菜品的口味信息，并保存再dishDto的flavor list中
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        //将数据list化
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(dishFlavorList);

        return dishDto;

    }


    /**
     * 修改菜品信息，同时更新对应的口味信息
     *
     * @param dishDto
     */
    @Override
    @Transactional//事务注解，保证事务的一致性
    public void updateWithFlavor(@RequestBody DishDto dishDto) {
        //将dish表信息更新
        this.updateById(dishDto);

        //更新dishflavor表信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId() );

        //清理口味
        dishFlavorService.remove(queryWrapper);

        //添加提交的口味信息
        List<DishFlavor> dishFlavors = dishDto.getFlavors();

        dishFlavors = dishFlavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavors);

    }

    /**
     * 更改菜品状态 禁用/启用
     * @param status
     * @param ids
     */
    @Transactional//事务注解，保证事务的一致性
    public void  status(Integer status,List<Long> ids){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //in id为？？的菜品项
        queryWrapper.in(ids!= null,Dish::getId,ids);
        List<Dish> dishList = this.list(queryWrapper);
        //对list中的dish项进行遍历 更改其状态
        for(Dish dish : dishList){
            dish.setStatus(status);
            this.updateById(dish);
        }
    }

    /**
     * 重写删除操作，因为 只有 停售的菜品才能被删除 status = 0
     * @param ids
     */

    /**
     * 根据 id 删除单个菜品或批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>();

        /* 查询该菜品的售卖状态。若为启售状态，则抛出业务异常；反之，则允许删除 */

        //添加条件
        queryWrapper.in(ids != null, Dish::getId, ids);

        List<Dish> list = this.list(queryWrapper);

        for (Dish dish : list) {
            Integer status = dish.getStatus();
            Long id = dish.getId();
            //若为停售状态，则可以删除
            if (status == 0) {
                this.removeById(dish.getId());

                LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.in(DishFlavor::getDishId, id);
                dishFlavorService.remove(queryWrapper1);
            } else {
                //此时应该回滚信息。防止出现恰好删除菜品时，该菜品也正在售卖中
                throw new CustomException("要删除的菜品中有正在售卖的菜品，无法全部删除");
            }
        }


    }

}
