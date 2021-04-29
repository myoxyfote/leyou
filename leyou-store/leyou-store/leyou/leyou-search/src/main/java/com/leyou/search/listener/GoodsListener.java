package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoodsListener {

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(value = "LEYOU-SEARCH-CREATE-QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU-ITEM-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.create","item.update"}
    ))
    public void listenCreate(Long id) throws IOException {
        if(id==null){
            return;
        }
        //创建或跟新索引
        searchService.createIndex(id);
    }


    /**
     * 处理delete的消息
     *
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU-SEARCH-DELETE-QUEUE", durable = "true"),
            exchange = @Exchange(
                    value = "LEYOU-ITEM-EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "item.delete"))
    public void listenDelete(Long id) {
        if (id == null) {
            return;
        }
        // 删除索引
        this.searchService.deleteIndex(id);
    }



}
