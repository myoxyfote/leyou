package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger LOGGER=LoggerFactory.getLogger(GoodsService.class);


    public PageResult<SpuBo> querySpuByPageAndSort(String key, Boolean saleable, Integer page, Integer rows){
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //有key的时候才执行模糊查询
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //上下架
        if(saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        //分页查询
        PageHelper.startPage(page,rows);
        //查询出来的结果没有分类名称和品牌名称
        List<Spu> spus = spuMapper.selectByExample(example);
        //初始化spubo集合
        List<SpuBo> spuBoList=new ArrayList<>();
        //
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        spus.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            //将spu的属性给spubo
            BeanUtils.copyProperties(spu,spuBo);
            //查询品牌
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            //查询商品三级分类 种类
            List<Category> categories = categoryMapper.selectByIdList(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            List<String > names=categories.stream().map(category -> category.getName()).collect(Collectors.toList());
            //分割
            String cname = StringUtils.join(names, "-");
            spuBo.setCname(cname);
            spuBoList.add(spuBo);
        });
        return new PageResult<>(pageInfo.getTotal(),spuBoList);
    }


    //保存添加的商品信息
    @Transactional
    public void save(SpuBo spuBo){

        //是否上架
        spuBo.setSaleable(true);
        //是否有效商品
        spuBo.setValid(true);
        //创建时间
        spuBo.setCreateTime(new Date());
        //跟新时间
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuMapper.insertSelective(spuBo);


        //添加spudetail
        //网页上传递的数据少了id 所以需要我们传过去
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insertSelective(spuDetail);

        List<Sku> skus = spuBo.getSkus();

        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(spuBo.getId());
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
        sendMessage(spuBo.getId(),"insert");
    }


    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(spuId);
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    //根据spuid查询商品细节
    public SpuDetail querySpuDetailById(Long id){
        return spuDetailMapper.selectByPrimaryKey(id);
    }

    //更具id查询sku
    public List<Sku> querySkuById(Long id){
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> list = skuMapper.select(record);
        list.forEach(sku->{
            //库存也需要回显
            sku.setStock(stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        });

        return list;

    }

    @Transactional
    public void update(SpuBo spuBo){
        // 更具spuid查询sku
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = skuMapper.select(record);
        //拿到skuid
        List<Long> skuIds = skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());

        //先删除库存
        Example stockExample = new Example(Stock.class);
        stockExample.createCriteria().andIn("skuId",skuIds);
        stockMapper.deleteByExample(stockExample);

        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        skuMapper.delete(sku);

        //跟新spu
        spuBo.setLastUpdateTime(new Date());
        spuMapper.updateByPrimaryKeySelective(spuBo);

        //更新spudetail
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        //保存库存和sku
        saveSkuAndStock(spuBo.getSkus(),spuBo.getId());

        sendMessage(spuBo.getId(),"update");
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }


    public void sendMessage(Long id,String type){
        try {
            amqpTemplate.convertAndSend("item."+type,id);
        } catch (AmqpException e) {
            e.printStackTrace();
            LOGGER.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }

    }


    public Sku findSkuById(Long id) {
        return skuMapper.selectByPrimaryKey(id);
    }
}
