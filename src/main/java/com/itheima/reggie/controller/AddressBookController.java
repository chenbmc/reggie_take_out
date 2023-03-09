package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
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

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import java.util.List;

/**
 * @Author:zyc
 * @Date:2023-03-01-10:13
 * @Deacription:
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        log.info("保存的地址信息：{}",addressBook);

        //获取session中的当前用户的id
        addressBook.setUserId(BaseContext.getCurrentId());

        addressBookService.save(addressBook);
        return R.success("保存成功");
    }

    /**
     * 获取当前用户的所有地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("获取地址列表",addressBook);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(),AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        log.info("设置默认地址");

        //先把所有该用户下的地址的 is_Default 设置为0 非默认
        //LambdaUpdateWrapper更新操作
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId() );
        updateWrapper.set(AddressBook::getIsDefault,0);
        //SQL: update address_book set is_default = 0 where user_id = ?
        addressBookService.update(updateWrapper);

        //设置当前地址id_default 为1
        addressBook.setIsDefault(1);
        //update address_book set is_default = 1 where user_id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);


    }

    /**
     * 地址的回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id){
        log.info("地址信息的回显");

        AddressBook addressBook = addressBookService.getById(id);

        if(addressBook!=null){
            return  R.success(addressBook);
        }else {
            return  R.error("没有该地址对象");
        }

    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> edit(@RequestBody AddressBook addressBook){
        log.info("返回数据“{}",addressBook);

        if(addressBook == null){
            return R.error("数据异常");
        }
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 删除地址
     * @param id
     * @return
     */

    @DeleteMapping
    public R<String> delete(@RequestParam("ids") Long id){
        log.info("删除地址");
        if(id == null){
            return R.error("请求异常");
        }
        //删除数据库的地址 不仅判断地址id还需 判断用户id？
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getId, id);
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId() );
        addressBookService.remove(queryWrapper);
        return R.success("删除成功");

    }
    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public R getDefaultAddress(){
        Long id  = BaseContext.getCurrentId();

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, id);
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        ////SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }
}
