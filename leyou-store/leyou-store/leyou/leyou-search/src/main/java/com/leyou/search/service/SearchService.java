package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    //我们要做的是将查询到的spu转换为goods
    public Goods buildGoods(Spu spu) throws IOException {
        //根据cid获取分类
        List<String> categoryNames = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //根据bid获取品牌
        Brand brand = brandClient.findBrandByBrandId(spu.getBrandId());

        //价格和skus都需要spu 通过spuid来获取skus
        List<Sku> skus = goodsClient.querySkuBySpuId(spu.getId());
        //这里的skus是string类型的 价格是list所以我们需要转成list
        List<Long> price = new ArrayList<>();
        //将sku放入list
        List<Map<String, Object>> skuMap = new ArrayList<>();

        //查询规格参数
        List<SpecParam> params = specificationClient.queryParams(null, spu.getCid3(), true, null);
        //规格参数详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());

        //取出通用规格和特殊规格 他们是string类型的 所以需要反序列化为map 因为表中的结构

        //反序列话为map key是参数的id 参数值 object
        Map<String, Object> genericSpec = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        //key是参数的id 参数值 list<object>
        Map<String, List<Object>> specialSpec = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });

        //遍历规格参数 获取对应的值 封装到map中  Map<String, Object>
        Map<String, Object> searchingMap = new HashMap<>();
        params.forEach(param -> {
            //判读参数是通用的还是特殊的
            if (param.getGeneric()) {
                //到通用的规格参数中取值 getkey 拿到值
                String value = genericSpec.get(param.getId().toString()).toString();
                //判读参数是否为数字
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                searchingMap.put(param.getName(), value);
            } else {
                //特殊规格参数
                List<Object> value = specialSpec.get(param.getId().toString());
                searchingMap.put(param.getName(), value);
            }

        });


        //遍历skus
        skus.forEach(sku -> {
            //价格集合
            price.add(sku.getPrice());
            //按需接受sku 我们需要的 id price image title
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("image", sku.getImages() == null ? " " : StringUtils.split(sku.getImages(), ","));
            map.put("price", sku.getPrice());
            skuMap.add(map);
        });


        //构建对象
        Goods goods = new Goods();
        //设置参数
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setBrandId(spu.getBrandId());
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        //搜索字段：标题+分类+品牌 join 将集合或者数组以什么拼接起来
        goods.setAll(spu.getTitle() + StringUtils.join(categoryNames, "-") + brand);
        //价格集合
        goods.setPrice(price);
        goods.setSkus(MAPPER.writeValueAsString(skuMap));
        goods.setSpecs(searchingMap);
        return goods;

    }



    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }



    public SearchResult search(SearchRequest request) {

        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (request==null || StringUtils.isBlank(request.getKey())) {
            return null;
        }
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        QueryBuilder basicQuery = QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND);
        //bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //基本查询条件放入bool查询
        boolQueryBuilder.must(basicQuery);

//        MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        Map<String, Object> filter = request.getFilter();

        //判断过滤条件是否为空
        if(!CollectionUtils.isEmpty(filter)){
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                String key = entry.getKey();
                if(StringUtils.equals(key,"品牌")){
                    key="brandId";
                }else if(StringUtils.equals(key,"分类")){
                    key="cid3";
                }else{
                    key="specs."+key+".keyword";
                }
                boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
            }
        }
        queryBuilder.withQuery(boolQueryBuilder);

        // 通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));

        Integer page = request.getPage();
        Integer size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page-1,size));

        // 聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms("category").field("brandId"));

        // 执行查询获取结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        System.out.println(goodsPage.getTotalPages());
        // 获取聚合结果集
        // 商品分类的聚合结果
        List<Category> categories = getCategoryAggResult(goodsPage.getAggregation("brands"));
        // 品牌的聚合结果
        List<Brand> brands = getBrandAggResult(goodsPage.getAggregation("category"));

        // 根据商品分类判断是否需要聚合
        List<Map<String, Object>> specs = new ArrayList<>();
        if (categories.size() == 1) {
            // 如果商品分类只有一个才进行聚合，并根据分类与基本查询条件聚合
            specs = getSpec(categories.get(0).getId(), basicQuery);
        }

        return new SearchResult(goodsPage.getTotalElements(), goodsPage.getTotalPages(), goodsPage.getContent(), categories, brands, specs);
    }

    /**
     * 聚合出规格参数
     *
     * @param cid
     * @param query
     * @return
     */
    private List<Map<String, Object>> getSpec(Long cid, QueryBuilder query) {
        try {
            // 不管是全局参数还是sku参数，只要是搜索参数，都根据分类id查询出来
            List<SpecParam> params = this.specificationClient.queryParams(null, cid, true, null);
            List<Map<String, Object>> specs = new ArrayList<>();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(query);

            // 聚合规格参数
            params.forEach(p -> {
                String key = p.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs." + key + ".keyword"));

            });

            // 查询
            Map<String, Aggregation> aggs = this.elasticsearchTemplate.query(queryBuilder.build(),
                    SearchResponse::getAggregations).asMap();

            // 解析聚合结果
            params.forEach(param -> {
                Map<String, Object> spec = new HashMap<>();
                String key = param.getName();
                spec.put("k", key);
                StringTerms terms = (StringTerms) aggs.get(key);
                spec.put("options", terms.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString));
                specs.add(spec);
            });

            return specs;
        } catch (
                Exception e)

        {
            logger.error("规格聚合出现异常：", e);
            return null;
        }

    }



    // 解析品牌聚合结果
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        try {
            LongTerms brandAgg = (LongTerms) aggregation;
            List<Long> bids = new ArrayList<>();
            for (LongTerms.Bucket bucket : brandAgg.getBuckets()) {
                bids.add(bucket.getKeyAsNumber().longValue());
            }
            if(CollectionUtils.isEmpty(bids)){
                return  new ArrayList<>();
            }
            // 根据id查询品牌
            return this.brandClient.queryBrandByIds(bids);
        } catch (Exception e) {
            logger.error("品牌聚合出现异常：", e);
            return null;
        }
    }

    // 解析商品分类聚合结果
    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        try {
            List<Category> categories = new ArrayList<>();
            LongTerms categoryAgg = (LongTerms) aggregation;
            List<Long> cids = new ArrayList<>();
            for (LongTerms.Bucket bucket : categoryAgg.getBuckets()) {
                cids.add(bucket.getKeyAsNumber().longValue());
            }

            if(CollectionUtils.isEmpty(cids)){
                return  new ArrayList<>();
            }
            // 根据id查询分类名称
            List<String> names = this.categoryClient.queryNameByIds(cids);

            for (int i = 0; i < names.size(); i++) {
                Category c = new Category();
                c.setId(cids.get(i));
                c.setName(names.get(i));
                categories.add(c);
            }
            return categories;
        } catch (Exception e) {
            logger.error("分类聚合出现异常：", e);
            return null;
        }
    }

    public void createIndex(Long id) throws IOException {

        Spu spu = this.goodsClient.querySpuById(id);
        // 构建商品
        Goods goods = this.buildGoods(spu);

        // 保存数据到索引库
        this.goodsRepository.save(goods);
    }

    public void deleteIndex(Long id) {
        this.goodsRepository.deleteById(id);
    }




}
