package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    //根据分页查询品牌
    @RequestMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(@RequestParam(value = "key", required = false) String key,
                                                              @RequestParam(value = "sortBy", required = false) String sortBy,
                                                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                              @RequestParam(value = "row", defaultValue = "5") Integer rows,
                                                              @RequestParam(value = "desc", required = false) Boolean desc
    ) {
        PageResult<Brand> result = brandService.queryBrandsByPage(key, sortBy, page, rows, desc);
        if (CollectionUtils.isEmpty(result.getItems())) {
            //找不到结果 响应404
        ResponseEntity.notFound().build();
        }
        //有结果 响应200
        return ResponseEntity.ok(result);
    }

    //新增请求
    @PostMapping
    public ResponseEntity<Void> save(Brand brand,@RequestParam("cids") List<Long> cids) {
        brandService.saveBrands(brand, cids);

        //201
        return new ResponseEntity<>(HttpStatus.CREATED);

    }


    @PutMapping
    public ResponseEntity<Void> updateBrandAndCategory(Brand brand,@RequestParam("cids") List<Long> cids){
        brandService.updateBrand(brand,cids);
        //204 修改成功
        return ResponseEntity.noContent().build();
    }




    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandListByCid(@PathVariable("cid")Long cid){

        List<Brand> brandList = this.brandService.queryBrandByCategory(cid);
        if(CollectionUtils.isEmpty(brandList)){
            // 响应404
            return ResponseEntity.notFound().build();
        }
        // 响应200
        return ResponseEntity.ok(brandList);
    }


    @GetMapping("{id}")
    public ResponseEntity<Brand> findBrandByBrandId(@PathVariable("id")Long id){

        Brand brand = brandService.findBrandByBrandId(id);
        if(brand==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brand);
    }

    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids){
        List<Brand> list = this.brandService.queryBrandByIds(ids);
        if(CollectionUtils.isEmpty(list)){
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }


}
