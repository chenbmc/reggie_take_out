package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.aop.aspectj.annotation.LazySingletonAspectInstanceFactoryDecorator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.print.DocFlavor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:zyc
 * @Date:2023-02-21-17:28
 * @Deacription:
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;
    /**
     * 分页查询
     *
     * 由于dish中没有categoryName 以至于无法显示分类
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();



        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件,对于菜品名 使用like查询 当name不为null 才添加这个过滤条件；这一句即实现了搜索框的查询功能
        queryWrapper.like(Strings.isNotEmpty(name),Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //思路：一个Page对象中有records列表属性，以及各个单独属性。
        //除了records列表属性，其他继承dish属性进行拷贝（注categoryName仍为空）
        BeanUtils.copyProperties(pageInfo, dishDtoPage,"records");

        //处理Dish对象的records列表，根据其categoryId 访问category表获取其categoryName，并给dishDto中的该属性赋值；
        List<Dish> records = pageInfo.getRecords();

        List<DishDto>dishDtoList = records.stream().map((item)->{
            //创建新的dishDto，将其继承的dish属性值全部拷贝。为什么是值？ pageInfo已经获取dish数据库的值
            DishDto dishDto = new DishDto();
            //该item是dish.records中的各个小属性
            BeanUtils.copyProperties(item, dishDto);

            //通过categoryId映射name
            Long categoryID = item.getCategoryId();
            Category category = categoryService.getById(categoryID);
            if(category != null){
                String categoryName = category.getName();
                //获取name并赋值
                dishDto.setCategoryName(categoryName);
            }



            return dishDto;
        }).collect(Collectors.toList());

        //把缺的含有categoryName的list补上
        dishDtoPage.setRecords(dishDtoList);

        //返回的是自建实体对象 包含dish属性和categoryName，以及更新后的list
        return R.success(dishDtoPage);
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        log.info("新增菜品，菜品信息：{}",dishDto.toString());

        //保存数据时候 需要操作两个表
        //mybatics 提供了save方法 则直接调用
        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");

    }

    /**
     * 修改信息时，菜品信息的回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改信息 对数据库的更新
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 菜品启售/停售（单个或批量
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") Integer status,@RequestParam  List<Long> ids) {
        log.info("status:{}", status);
        log.info("ids:{}", ids);

       dishService.status(status, ids);

        return R.success("售卖状态修改成功");
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids={}",ids);
        //删除菜品，重写的方法中有判断状态的
        dishService.deleteByIds(ids);

        return R.success("菜品删除成功");
    }

    /**
     * 添加套餐页面，展示菜品种类下面的菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>();
        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId, dish.getCategoryId());
        //查询状态为1 启售状态
        queryWrapper.eq(Dish::getStatus,1);

        queryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);


        List<DishDto> dishDtoList  = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            //设置categoryName
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);}
            //设置口味信息
            Long dishId = item.getId();

            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dishId);
            dishDto.setFlavors(dishFlavorService.list(queryWrapper1));

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }


}
