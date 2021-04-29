package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import sun.reflect.generics.tree.VoidDescriptor;

import java.util.List;

@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;


    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        Spu spu = this.goodsService.querySpuById(id);
        if(spu==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spu);
    }


    // 请求方法及路径
    @GetMapping("spu/page")
    //返回值类型及参数
    public ResponseEntity<PageResult<SpuBo>> querySpusByPage(@RequestParam(value = "key",required = false) String key,
                                                                   @RequestParam(value = "saleable",required = false) Boolean saleable,
                                                                   @RequestParam(value = "page",defaultValue = "1") Integer page,
                                                                   @RequestParam(value = "rows",defaultValue = "5") Integer rows){

        PageResult<SpuBo> spuBoPageResult = goodsService.querySpuByPageAndSort(key, saleable, page, rows);


        return ResponseEntity.ok().body(spuBoPageResult);

    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){

        goodsService.save(spuBo);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id") Long id){
        SpuDetail spuDetail = goodsService.querySpuDetailById(id);
        if(spuDetail==null){
            // 找不到资源 404
            return ResponseEntity.notFound().build();
        }
        return  ResponseEntity.ok(spuDetail);
    }

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuById(@RequestParam("id") Long id){
        List<Sku> sku = goodsService.querySkuById(id);
        System.out.println(sku);
        if(CollectionUtils.isEmpty(sku)){
            //找不到资源 404
            return  ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }

    @PutMapping("goods")
    public ResponseEntity<Void> update(@RequestBody SpuBo spuBo){
        goodsService.update(spuBo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> findSkuById(@PathVariable("id")Long id){
        Sku sku = this.goodsService.findSkuById(id);
        if (sku == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(sku);
    }





}
