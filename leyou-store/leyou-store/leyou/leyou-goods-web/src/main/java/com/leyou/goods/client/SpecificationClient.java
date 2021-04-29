package com.leyou.goods.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("leyou-service")
public interface SpecificationClient extends SpecificationApi {
}
