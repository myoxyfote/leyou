package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>,SelectByIdListMapper<Brand,Long> {

    //给中间表添加数据
    @Insert("insert into tb_category_brand (category_id,brand_id) values (#{cid},#{bid})")
    void save(@Param("cid") Long cid, @Param("bid") Long bid);

    //删除中间表
    @Delete("delete from tb_category_brand where brand_id=#{bid}")
    void deleteBrandAndCategory(@Param("bid") Long bid);

    @Select("SELECT b.* FROM tb_brand b LEFT JOIN tb_category_brand cb ON b.id = cb.brand_id WHERE cb.category_id = #{cid}")
    List<Brand> queryByCategoryId(Long cid);
}
