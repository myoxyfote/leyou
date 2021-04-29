package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    // 根据pid查询商品种类
    public List<Category> queryCategoryByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        return categoryMapper.select(category);
    }

    public List<Category> queryBrandByBid(Long bid) {
        return  categoryMapper.queryBrandBybid(bid);

    }


    //删除品牌
    @Transactional
    public void deleteBrand(Long bid){
        //先删除中间表中的数据
        categoryMapper.deleteBrandAndCategory(bid);
        //删除brand中的数据
        categoryMapper.deleteBrand(bid);

    }

    public List<String> findCategoryByIds(List<Long> ids){
        return categoryMapper.selectByIdList(ids).stream().map(category -> category.getName()).collect(Collectors.toList());
    }


}
