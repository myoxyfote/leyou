package com.leyou.goods.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("leyou-service")
public interface CategoryClient extends CategoryApi {
}
