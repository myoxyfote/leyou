package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    //根据分类查询规格组
    public List<SpecGroup> queryGroups(Long cid){
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return specGroupMapper.select(specGroup);
    }

    //根据组id查询规格参数
    public List<SpecParam> queryParam(Long gid,Long cid,Boolean searching,Boolean generic){
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        specParam.setGeneric(generic);
        return specParamMapper.select(specParam);
    }


    public List<SpecGroup> querySpecsByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> groups = this.queryGroups(cid);
        SpecParam param = new SpecParam();
        groups.forEach(g -> {
            // 查询组内参数
            g.setParams(this.queryParam(g.getId(), null, null, null));
        });
        return groups;
    }

}
