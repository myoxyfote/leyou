package com.leyou.goods.service;

import com.leyou.goods.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {

    //模版引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private GoodsService goodsService;

    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);


    public void createHtml(Long spuId){

        PrintWriter printWriter=null;

        try {
            //service 获取页面数据
            Map<String, Object> data = goodsService.loadData(spuId);
            //创建上下文对象
            Context context = new Context();
            //将数据放入上下文
            context.setVariables(data);

            //创建输出流
            File file = new File("/usr/local/var/www/item/"+spuId+".html");

          printWriter = new PrintWriter(file);


            //执行页面静态化方法
            templateEngine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            LOGGER.error("页面静态化出错:{},"+e,spuId);
        }finally {
            if(printWriter!=null){
                printWriter.close();;
            }
        }
    }

    /**
     * 新建线程处理页面静态化
     * @param spuId
     */
    public void asyncExcute(Long spuId) {
        ThreadUtils.execute(()->createHtml(spuId));

    }

    public void deleteHtml(Long id) {
        File file = new File("/usr/local/var/www/item/"+id+".html");
        file.deleteOnExit();

    }
}
