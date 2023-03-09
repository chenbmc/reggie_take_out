package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 *
 * 实现文件的上传和下载
 * @Author:zyc
 * @Date:2023-02-21-15:35
 * @Deacription:
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    //转存路径声明
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件的上传
     * MultipartFile 是固定的
     * file 也需要进去前端传回的文件名 一致
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要更改其存储路径，不然的话 请求结束后 图片就消失了
        log.info(file.toString());

        //原始文件名,由于有可能重名覆盖 则不适用
        String originalFileName = file.getOriginalFilename(); //abc.jpg
        String suffix = originalFileName.substring(originalFileName
                .lastIndexOf(".")); //将后缀jpg截出来


        //使用UUID重新生成文件名，防止重名覆盖
        String fileName = UUID.randomUUID().toString() + suffix; //jpg加入后面

        //创建目录对象
        File dir = new File(basePath);
        //判断目录是否存在
        if(!dir.exists()){
            //目录不存在 需要创建
            dir.mkdirs();

        }
        try{
            //将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));

        }catch(IOException e){
            e.printStackTrace();
        }

        //新增菜品时，需要将图片的名称发送给页面，最后展现的时候用
        return R.success(fileName);
    }

    /**
     * 文件下载
     *
     * response：输出流需要通过response获得 即响应
     * @param name 上传时候 传入的文件名称
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流 读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流 将文件写回浏览器，展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            //告诉输出流 流的类型
            response.setContentType("image/jpeg");

            //进行流的读写
            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes))!= -1){
                outputStream.write(bytes,0,len);
                //写完刷新
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
