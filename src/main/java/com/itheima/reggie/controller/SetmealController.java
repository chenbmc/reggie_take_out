package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import jdk.jshell.Snippet;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @Author:zyc
 * @Date:2023-02-23-10:52
 * @Deacription:
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 分页查询， 涉及到category表和setmeal表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("分页查询，name = {}",name);

        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //查询套餐信息表，将表中信息 赋值给setmealDto
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Strings.isNotEmpty(name), Setmeal::getName,name);
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页查询
        setmealService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo, setmealDtoPage,"records");

        List<Setmeal> setmealList = pageInfo.getRecords();
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            //根据categoryid找categoryname
            Long categoryId = item.getCategoryId();
            //根据分类id 查对象
            Category category = categoryService.getById(categoryId);

            if(category!=null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;

        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(setmealDtoList);
        return R.success(setmealDtoPage);
    }
    /**
     * 添加套餐
     *
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("添加套餐");

        setmealService.saveWithDish(setmealDto);
        return R.success("新增菜品成功");
    }
    /**
     * 获取 套餐信息 信息回显
     *
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息，同时更新对应的菜品信息
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());

        if (setmealDto == null) {
            return R.error("请求异常");
        }

        if (setmealDto.getSetmealDishes() == null) {
            return R.error("套餐没有菜品,请添加菜品到套餐中");
        }

        setmealService.updateWithDish(setmealDto);

        return R.success("修改菜品成功");
    }

    /**
     * 删除套餐 同时删除关联信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> removeWithDish(List<Long> ids){
        log.info("删除套餐，ids={}",ids);

        setmealService.removeWithDish(ids);

        return  R.success("删除成功");

    }

    /**
     * 更改套餐状态
     * @param status
     * @param ids
     * @return
     */

    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") Integer status,List<Long> ids){
        setmealService.status(status, ids);
        return R.success("更改状态成功");
    }

    /**
     * 客户端 展示套餐分类
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null, Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 客户端 点击套餐图片时候 查看 套餐详细信息 包括菜品的份数等
     * @param setmealId
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long setmealId){
        LambdaQueryWrapper<SetmealDish>queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId );
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtoList  = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            //最关键的 cpoies 份数 复制进去
            BeanUtils.copyProperties(item, dishDto);

            //其他菜品信息复制进去
            Long dishId = item.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

}
