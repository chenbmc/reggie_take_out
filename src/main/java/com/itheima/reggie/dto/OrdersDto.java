package com.itheima.reggie.dto;

import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:zyc
 * @Date:2023-03-06-15:41
 * @Deacription:
 */
@Data
public class OrdersDto extends Orders {
    private List<OrderDetail> orderDetails = new ArrayList<>();
    //private Integer sumNum;
    private String userName;

}
