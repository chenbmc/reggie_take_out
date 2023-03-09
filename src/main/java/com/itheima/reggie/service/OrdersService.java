package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Orders;

/**
 * @Author:zyc
 * @Date:2023-03-06-9:50
 * @Deacription:
 */
public interface OrdersService extends IService<Orders> {
    public void submit(Orders orders);

    //用户查看自己的订单信息
    public void page(Page<Orders> ordersPageInfo, Page<OrdersDto> ordersDtoPageInfo);

}
