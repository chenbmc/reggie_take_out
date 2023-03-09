package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Request;
import org.springframework.util.AntPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 页面请求过滤
 */
//定义该过滤器的名称，和过滤的url
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //spring提供的一个方法，路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //向下转型，request服务请求和服务响应

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}",requestURI);
        //定义不需要处理的请求路径
        String[] urls = new String[]{
          "/employee/login",
          "/employee/logout",
          "/backend/**",
          "/front/**",
          "/common/**",
          "/user/sendMsg",//移动端发送端行
           "/user/login" //移动端登陆
        };
        //2.判断本次请求使否需要处理
        boolean check = check(urls, requestURI);

        //3.如check-true 不需要处理即放行
        if(check){
            log.info("本次请求不需要处理：{}",requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4.若false 则判断用户是否登录，如登录 直接放行
        if(request.getSession().getAttribute("employee")!=null){
            //检验线程id代码
            long id = Thread.currentThread().getId();
            log.info("线程id：{}",id);
            //ThreadLocal获取登录用户id
            Long empId = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            log.info("用户已经登录：{}", request.getSession().getAttribute("employee"));
            filterChain.doFilter(request, response);
            return;
        }
        //4.2 判断 客户端用户登录
        if(request.getSession().getAttribute("user")!=null){
            //检验线程id代码
            long id = Thread.currentThread().getId();
            log.info("线程id：{}",id);
            //ThreadLocal获取登录用户id
            Long empId = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(empId);

            log.info("客户端用户已经登录：{}", request.getSession().getAttribute("user"));
            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录");
        //5. 如果未登录 则返回未登录结果，通过输出流方式 向客户端页面响应数据
        //backend中index.html 引用了request.js文件，里面定义了拦截器的相关设置，返回相关数据，js文件里自动跳转到了登录页面
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     *路径匹配，声明在第一行 检查请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for(String url:urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }return false;

    }
}
