package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("brand")
public interface BrandApi {



    @GetMapping("list")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);


    @GetMapping("{id}")
    public Brand findBrandByBrandId(@PathVariable("id") Long id);

}
