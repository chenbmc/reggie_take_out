package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author:zyc
 * @Date:2023-03-02-14:09
 * @Deacription:
 */
@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品/套餐到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加菜品/套餐");
        //设置当前用户的id
        shoppingCart.setUserId(BaseContext.getCurrentId());


        //添加的 有可能是菜品 有可能是套餐,设置 查询条件 为之后的添加重复做准备
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        Long dishId = shoppingCart.getDishId();
        if(dishId !=null){
            //添加的是菜品信息
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }else{
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if(cartServiceOne != null){
            //之前已经加入 则数量加1
            Integer num = cartServiceOne.getNumber();
            cartServiceOne.setNumber(num+1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //未加入
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }

        return R.success(cartServiceOne);
    }

    /**
     * 展示购物车信息
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**、
     * 减少 购物车菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("减少 菜品/套餐份数");

        //获取用户id
        Long userId = BaseContext.getCurrentId();


        //查询条件 该用户下的菜品信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        Long dishId = shoppingCart.getDishId();
        //不为空 操作的是菜品
        if(dishId !=null){
            queryWrapper.eq(ShoppingCart::getDishId, dishId);

        }else {
            //操作的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId() );
        }
        //符合条件的 数据获取
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
        //获取 菜品//套餐 的数量
        Integer num = shoppingCart1.getNumber();
        //如果数量大于1
        if(num!=1){
            //把数量-1
            shoppingCart1.setNumber(num-1);
            shoppingCartService.updateById(shoppingCart1);
        }else{
            //如果数量为 直接删除该条数据
            shoppingCartService.remove(queryWrapper);
        }
        return R.success(shoppingCart1);

    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
