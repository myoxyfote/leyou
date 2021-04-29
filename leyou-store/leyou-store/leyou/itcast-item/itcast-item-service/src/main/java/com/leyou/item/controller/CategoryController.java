package com.leyou.item.controller;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam("pid") Long pid) {

        //  可以省略trycatch 因为有异常的话会自动响应500

        if (pid == null || pid < 0) {
            //参数不符合 响应400
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = categoryService.queryCategoryByPid(pid);
        if (CollectionUtils.isEmpty(categories)) {
            // (对象为空)找不到资源 响应404
            return ResponseEntity.notFound().build();
        }
        // 一切ok 响应200
        return ResponseEntity.ok(categories);

    }


    //更具商品id查找种类
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryBrandByBid(@PathVariable("bid")Long bid){
        List<Category> categories = categoryService.queryBrandByBid(bid);
        //找不到资源
        if(CollectionUtils.isEmpty(categories)){
        return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }


    //删除品牌
    @DeleteMapping("bid/{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long bid){
        categoryService.deleteBrand(bid);
        //204 删除成功
        return ResponseEntity.noContent().build();
    }

    //查询商品分类名称
    @GetMapping("query")
    public ResponseEntity<List<String>> findCategoryByIds(@RequestParam("ids")List<Long> ids){
        List<String> categorys = categoryService.findCategoryByIds(ids);
        if(CollectionUtils.isEmpty(categorys)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categorys);
    }



}
