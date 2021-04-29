package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;


    //分页查询品牌
    public PageResult<Brand> queryBrandsByPage(String key,String sortBy,Integer page,Integer rows,Boolean desc){
        Example example = new Example(Brand.class);

        //查询功能
        if(StringUtils.isNotBlank(key)){
          example.createCriteria().andLike("name","%"+key+"%").orEqualTo("letter",key);

        }

        //分页
        PageHelper.startPage(page,rows);

        //排序
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+(desc?" desc":" asc"));
        }

        //执行查询
        List<Brand> list = brandMapper.selectByExample(example);

        //获取分页信息
        PageInfo<Brand> pageInfo = new PageInfo<>(list);

        PageResult<Brand> pageResult = new PageResult<>();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setItems(pageInfo.getList());


        return pageResult;
    }

    //开启事物
    @Transactional
    public void saveBrands(Brand brand,List<Long> cids){
        //插入数据
        brandMapper.insertSelective(brand);
        //给中间数组注入值
        cids.forEach(id->{
            brandMapper.save(id,brand.getId());
        });

    }

    @Transactional
    public void updateBrand(Brand brand,List<Long> cids){
        //修改品牌中的信息
        brandMapper.updateByPrimaryKeySelective(brand);
        //删除中间表的数据
        brandMapper.deleteBrandAndCategory(brand.getId());
        //插入中间表数据

        //给中间数组注入值
        cids.forEach(id->{
            brandMapper.save(id,brand.getId());
        });

    }


    public List<Brand> queryBrandByCategory(Long cid) {
        return this.brandMapper.queryByCategoryId(cid);
    }


    public Brand findBrandByBrandId(Long id){
        return brandMapper.selectByPrimaryKey(id);
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        return brandMapper.selectByIdList(ids);
    }
}
