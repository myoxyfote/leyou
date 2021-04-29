package com.leyou.goods.listener;

import com.leyou.goods.service.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

@Component
public class GoodsListener {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU-GOODS-CREATE-QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU-ITEM-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key={"item.create","item.update"}
    ))
    public void listenCreate(Long id){
        if(id==null){
            return;
        }
        goodsHtmlService.createHtml(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU-GOODS-DELETE-QUEUE",durable="true"),
            exchange = @Exchange(value = "LEYOU-ITEM-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}

    ))
    public void linstenDelete(Long id){
        if (id == null) {
            return;
        }
        // 删除页面
        goodsHtmlService.deleteHtml(id);
    }

}
