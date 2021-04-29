package com.leyou.goods.controller;

import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class GoodsController {


    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsHtmlService goodsHtmlService;


    @GetMapping("{id}.html")
    public String toItemImage(Model model,@PathVariable("id") Long id){
        Map<String, Object> modelMap = goodsService.loadData(id);

        model.addAllAttributes(modelMap);

        //线程解决并发
        goodsHtmlService.asyncExcute(id);


        return "item";
    }
}
