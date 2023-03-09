package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:zyc
 * @Date:2023-03-06-9:54
 * @Deacription:
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单提交支付");
        ordersService.submit(orders);
        return R.success("下单成功");

    }

    /**
     * 用户查看订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){
        Page<Orders> pageInfo = new Page<Orders>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        ordersService.page(pageInfo,ordersDtoPage);
        return R.success(ordersDtoPage);
    }

    /**
     * 后台 订单明细，
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page2(int page,int pageSize,String number, String beginTime,String endTime){
        log.info("分页查询 订单信息");
        //构造分页构造器
        Page<Orders> ordersPage = new Page<Orders>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<OrdersDto>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //模糊查询 订单id（string）查询
        queryWrapper.eq(!StringUtils.isEmpty(number), Orders::getNumber, number);
        //范围查询
        if(beginTime!=null||endTime!=null){
            queryWrapper.ge(Orders::getOrderTime, beginTime);//ge 大于等于
            queryWrapper.le(Orders::getOrderTime, endTime);//le 小于等于
        }
        //排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //进行分页查询
        ordersService.page(ordersPage, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(ordersPage, ordersDtoPage,"records");

        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> ordersDtoList = records.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            String name = item.getConsignee();
            ordersDto.setUserName(name);

            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }

    /**
     * 后台更改 订单状态 3 已派送 4已完成 完成之后 客户可在前端 进行再来一单
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> editStatus(@RequestBody Orders orders){
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId, orders.getId());
        Orders orders1 = ordersService.getOne(queryWrapper);
        orders1.setStatus(orders.getStatus());
        ordersService.updateById(orders1);
        return R.success("派送成功");
    }

    /**
     * 再来 一单
     * 接收到 orderid，在orderDeatail表中获取 该oder的菜品信息，将菜品信息 插入到shoppingCart 购物车表中即可。
     * @param orders1
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders1){
        //获取订单ID
        Long id = orders1.getId();
        //查询是否有该订单信息
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId, id );
        Orders orders = ordersService.getOne(queryWrapper);
        //有该订单
        if(orders!=null){

            //查询OrderDetail表中 该订单的菜品/套餐信息
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, id);
            List<OrderDetail> list = orderDetailService.list(orderDetailLambdaQueryWrapper);
            //复制信息到购物车中
            List<ShoppingCart> shoppingCartList = list.stream().map((item)->{
                Long userId = BaseContext.getCurrentId();
                ShoppingCart shoppingCart = new ShoppingCart();
                //复制userid
                shoppingCart.setUserId( userId);
                //必要信息
                BeanUtils.copyProperties(item, shoppingCart);
                //开单时间
                shoppingCart.setCreateTime(LocalDateTime.now());
                return shoppingCart;
            }).collect(Collectors.toList());
            //保存批量数据
            shoppingCartService.saveBatch(shoppingCartList);

        }
        else{
            return R.error("订单信息丢失");
        }
        return R.success("再来一单");

    }

}
