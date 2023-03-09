package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * pring中@RestController的作用等同于@Controller + @ResponseBody。
 * 如果要求方法返回的是json格式数据，而不是跳转页面，可以直接在类上标注@RestController
 * @Autowired 将service接口注入进来
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 编写员工登录功能
     * 输入用户名和密码，密码加密 传入数据库检测 成功之后返回
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码password进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据页面提交的username查询数据库，mybatics-plus的要求
        //定义一个Employee类的查询对象
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //设定查询条件
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //查出的结果封装成Employee类，查询所用工具即Service接口提供，类中第一行即将接口传入
        Employee emp = employeeService.getOne(queryWrapper);

        //3.如果没有查到用户名 返回登陆失败
        if(emp == null){
            //结果封装成R对象，因为要返回到页面
            return R.error("登陆失败");
        }
        //4.比对密码，数据库查出来的 和 加密后的
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //5.查看员工状态 是否禁用
        if(emp.getStatus() == 0 ){
            return  R.error("账号已禁用");
        }
        //6.登录成功，将用户ID存入session中(类似于网页中的存储区)
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);

    }
    @PostMapping("/logout")
    /**
     * 员工退出
     * 前端请求到这，后端服务进行操作
     * 需求：清理Session中保存的当前登录员工的id
     */
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 添加员工
     * 由于添加员工请求路径就是/employee 则@postmapping不用加路径了
     * @param employee
     * @return
     * @RequestBody Employee employee 加前面的原因是 传回来的数据使json形式
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());

        //将前端传的数据接收到，没有传的数据手动设置

        //设置初始密码 添加员工时 没有输入密码选项，需要md5加密处理 123456，而激活状态不需要设置 默认1
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        /**
         * 公共字段填充设置，只需在设置方法统一的进行公共字段的更新
         */
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //获取创建人id，创建人信息在session里面 则需要request对象
//        //通过下面方法得到的数据使Object类型，则需要向下转型
//        Long empId = (Long)request.getSession().getAttribute("employee") ;
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        //都设置好之后，controller调用service接口 与DB进行通信
        //mybatics 提供了save方法 则直接调用
        employeeService.save(employee);

        return R.success("新增员工成功");

    }

    /**
     * 员工信息分页查询
     * 条件：分页插件 commom/GlobalExceptionHandler
     * <Page>泛型 由mybaticsplus提供，（用在分页查询）根据前端页面代码 需要后端反回的数据决定泛型是什么
     * await getMemberList(params).then(res => {
     *               if (String(res.code) === '1') {
     *                 this.tableData = res.data.records || []
     *                 this.counts = res.data.total
     *               }
     * @param page 默认1
     * @param pageSize 默认10 （前端vue设定的默认值）
     * @param name

     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构造分页构造器，封装page pagesize，告诉mubaticsplus的分页插件 这两个值
        Page pageInfo = new Page(page,pageSize);

        //调用mybatics Plus之后，下面即它的要求
        //构造条件构造器，封装条件(对数据库的查询操作)
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件,对于姓名 使用like查询 当name不为null 才添加这个过滤条件
        queryWrapper.like(Strings.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询Service page方法将我们传进去的数据pageInfo进行了封装（Page型，里面由records total等数据）

        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 更新状态等信息 集成到了一个方法，更新数据库信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        //检验线程id
        long id = Thread.currentThread().getId();
        log.info("线程id：{}",id);

        /**
         * 公共字段填充设置，只需在设置方法统一的进行公共字段的更新
         */
        //需要注明更新人和更新时间
//        Long empId = (Long)request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);
        return R.success("信息修改成功");
    }

    /**
     * 修改员工信息时的数据回显
     * @PathVariable 注释意思是：传入的id在请求路径里
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息");
        Employee emp = employeeService.getById(id);
        if(emp != null){return R.success(emp);}
        return R.error("没有查询到对应员工信息");

    }

}
