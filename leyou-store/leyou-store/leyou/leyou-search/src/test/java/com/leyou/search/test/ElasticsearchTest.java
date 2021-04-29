package com.leyou.search.test;

import com.github.andrewoma.dexx.collection.internal.base.Break;
import com.leyou.SearchApplication;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import com.netflix.discovery.converters.Auto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class ElasticsearchTest {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private SearchService searchService;


    @Test
    public void createIndex() {
        //创建索引库
        elasticsearchTemplate.createIndex(Goods.class);
        //创建映射
        elasticsearchTemplate.putMapping(Goods.class);

        Integer page = 1;
        Integer rows = 100;


        do {
            PageResult<SpuBo> result = goodsClient.querySpuByPage(null, true, page, rows);
            List<SpuBo> spuBo = result.getItems();

            List<Goods> goodsList=new ArrayList<>();

//            spuBo.forEach(spu->{

//                try {
//                  Goods  goods = searchService.buildGoods(spu);
//                    goodsList.add(goods);
//                } catch (IOException e) {
//
//                }
//
//            });


            for (SpuBo spu : spuBo) {
                try {
                    Goods goods = this.searchService.buildGoods(spu);
                    goodsList.add(goods);
                } catch (Exception e) {
                    break;
                }
            }

            goodsRepository.saveAll(goodsList);
            rows= spuBo.size();
        page++;

        } while (rows == 100);
    }

}
