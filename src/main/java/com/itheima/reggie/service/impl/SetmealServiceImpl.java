package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:zyc
 * @Date:2023-02-20-14:36
 * @Deacription:
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 添加套餐信息 同时更新 setmeal表 和保存 套餐id对应的菜品信息 （set meal dish表中）
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(@RequestBody SetmealDto setmealDto){
        //保存setmeal基本信息
        this.save(setmealDto);

        //保存setmealDish表中的信息，所得信息为batch集合,但该集合中没有 setmeal_id 所以对集合进行处理
        Long setmealDtoId = setmealDto.getId();

        //对dto集合数据进行处理 setmealDish表中有setmealID 所以将dto集合赋给setmealDish 再使每个item 中的SetmealId进行赋值
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDtoId);
            return item;
        }).collect(Collectors.toList());

        //保存菜的口味 到setmealDish表中 且对应setmealId
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 修改套餐信息 信息回显
     * @param id
     * @return
     */
    @Transactional
    public SetmealDto getByIdWithDish( Long id){
        //获取套餐setmeal表中 该id的信息
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        //根据套餐id 查询在套餐菜品表中 对应的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;

    }
    /**
     * 更新套餐信息，同时更新对应的菜品信息
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto){
        //将setmeal的信息 更新了
        this.updateById(setmealDto);
        //更新setmealDish表信息
        //删除setmeal表中 对应setmealid的所有数据 即 setmeal_dish 表的 delete 操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId() );
        setmealDishService.remove(queryWrapper);

        //添加 当前提交的菜品数据，即 setmeal_dish 表的 insert 操作
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        list = list.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(list);
    }

    /**
     * 删除套餐 同时删除关联信息
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);
        if(count > 0 ){
            throw new CustomException("有的套餐正在售卖，不能删除");
        }
        //如果能删除 删除
        this.removeByIds(ids);

        //删除 setmealDish 中 套餐关联的数据
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper1);
    }

    /**
     * 状态更改
     * @param status
     * @param ids
     */
    @Transactional
    public void status(Integer status,List<Long> ids){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> list = this.list(queryWrapper);

        for(Setmeal item:list){
            if(item != null){
                item.setStatus(status);
                this.updateById(item);
            }

        }
    }

}
