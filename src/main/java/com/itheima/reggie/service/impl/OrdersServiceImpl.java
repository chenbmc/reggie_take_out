package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.mapper.SetmealDishMapper;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.ShoppingCartService;
import com.itheima.reggie.service.UserService;
import jdk.jfr.consumer.RecordedStackTrace;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author:zyc
 * @Date:2023-03-06-9:52
 * @Deacription:
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Transactional
    //@Transactional 保证 事务的原子性， 如果插入一张表成功 另一张失败 则会回滚 插入成功的数据也删除
    public void submit(Orders orders) {
        Long userId = BaseContext.getCurrentId();
        Long orderId = IdWorker.getId();//订单号
        //获取 订单金额
        AtomicInteger amount = new AtomicInteger(0);
        //查询用户信息
        User user = userService.getById(userId);
        //查地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }
        //购物车信息，保存订单详细信息；下单完成后 对购物车进行清理
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        if(list == null || list.size() == 0){
            throw new CustomException("购物车为空,请添加菜品");
        }
        List<OrderDetail> list1 = list.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            //单价乘以份数
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());

            return orderDetail;
        }).collect(Collectors.toList());

        //对 Dorder表的操作
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserId(userId);//下单用户 ID
        orders.setUserName(user.getName()); //下单用户姓名
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        //从 订单详情表操作中 获取总金额
        orders.setAmount(new BigDecimal(amount.get()));

        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                        + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                        + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                        + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //保存 Orders表数据 一条数据
        this.save(orders);
        //保存ordersDetail表数据 多条数据
        orderDetailService.saveBatch(list1);
        //下单完成后 对购物车数据进行清扫
        shoppingCartService.remove(wrapper);

    }
    @Transactional
    /**
     * 对（历史）订单进行显示
     */
    public void page(Page<Orders> ordersPageInfo, Page<OrdersDto> ordersDtoPageInfo){
        //查询订单 信息，查询该用户下的订单信息
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //这里是直接把当前用户分页的全部结果查询出来，要添加用户 id 作为查询条件，否则会出现用户可以查询到其他用户的订单的情况
        queryWrapper.eq(Orders::getUserId  , BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //分页查询
        ordersService.page(ordersPageInfo,queryWrapper);
        //对 OrderDto 进行必要的属性赋值
        List<Orders> records = ordersPageInfo.getRecords();
        List<OrdersDto> ordersDtoList = records.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();
            //获取orderdetail 信息
            Long orderId = item.getId();
            //通过订单的 id 来查询订单明细，得到一个订单明细的集合
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> list = orderDetailService.list(wrapper);

            //ordersDto 中的 orders信息拷贝 //为 orderDto 里面的属性赋值
            BeanUtils.copyProperties(item, ordersDto);
            //ordersDto 中的 orderDetailList信息拷贝 //对 orderDto 进行 OrderDetails 属性的赋值
            ordersDto.setOrderDetails(list);
            return ordersDto;

        }).collect(Collectors.toList());
        //dto 除records 外 其他基本信息赋值
        BeanUtils.copyProperties(ordersPageInfo,ordersDtoPageInfo,"records");
        //dto records 赋值
        ordersDtoPageInfo.setRecords(ordersDtoList);
    }
}
