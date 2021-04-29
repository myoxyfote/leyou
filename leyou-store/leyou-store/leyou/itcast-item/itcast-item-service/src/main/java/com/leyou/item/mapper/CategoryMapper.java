package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.DeleteMapping;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category,Long> {

    @Select("select * from tb_category where id in (select category_id from tb_category_brand where brand_id = #{bid})")
    List<Category> queryBrandBybid(Long bid);


    //删除中间表
    @Delete("delete from tb_category_brand where brand_id=#{bid}")
    void deleteBrandAndCategory(@Param("bid") Long bid);


    //删除brand
    @Delete("delete from tb_brand where id=#{bid}")
    void deleteBrand(@Param("bid") Long bid);

}
