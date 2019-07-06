package com.target.retail.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.target.retail.api.dao.RetailDao;
import com.target.retail.api.entity.Product;
import com.target.retail.api.model.Error;
import com.target.retail.api.model.Price;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
public class RetailServiceImpl implements RetailService {

    @Value("${api.product.url}")
    String productUrl;

    @Autowired
    RetailDao retailDao;

    @Override
    public ResponseEntity<Object> getProductDetails(String id) {
        ResponseEntity<Object> responseEntity = null;
        Object product = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (!StringUtils.isEmpty(id) && StringUtils.isNumeric(id)) {
                /*Product productEntity = productRepository.getProductPrice(Long.valueOf(id));
                Map<String, Object> productMap = getProductInfo(id);
                product = populateProductDetails(productEntity,productMap);*/
                Observable<Optional<Product>> productDetailsObservable = Observable.<Optional<Product>>create(subscriber -> {
                    subscriber.onNext(Optional.ofNullable(retailDao.getProductPrice(Long.valueOf(id))));
                    subscriber.onComplete();
                });
                Observable<Optional<Map<String, Object>>> productApiObservable = Observable.<Optional<Map<String, Object>>>create(subscriber -> {
                    subscriber.onNext(Optional.ofNullable(getProductInfo(id)));
                    subscriber.onComplete();
                });
                Observable<Object> productResponse = Observable.zip(productDetailsObservable, productApiObservable,
                        (productEntity, productApiDetails) -> populateProductDetails(productEntity.orElse(null), productApiDetails.orElse(null)));

                product = productResponse.observeOn(Schedulers.io()).blockingFirst();
                responseEntity = new ResponseEntity<>(product, HttpStatus.OK);
            } else {
                responseEntity = new ResponseEntity<>(populateErrorMessage("Validation failed invalid id. Id is either null or not numeric "), HttpStatus.PRECONDITION_FAILED);
            }
        } catch (Exception e) {
            log.info("label=getProductDetails exception={}", e.getMessage());
            responseEntity = new ResponseEntity<>(populateErrorMessage("An exception occured while fetching product details: "+e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }


    @Override
    public ResponseEntity<Object> updateProductDetails(String id, com.target.retail.api.model.Product product) {
        try {
            if (!StringUtils.isEmpty(id) && StringUtils.isNumeric(id) && product != null && product.getPrice() != null) {
                Product productEntity = retailDao.getProductPrice(Long.valueOf(id));
                if (productEntity != null) {
                    BigDecimal price = new BigDecimal(product.getPrice().getValue());
                    productEntity.setPrice(price);
                    //productRepository.save(productEntity);
                    retailDao.updateProductDetails(productEntity);
                } else {
                    return new ResponseEntity<>(populateErrorMessage("No product details found for the id:" + id), HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception e) {
            log.error("lable=updateProductDetails an exception occured while saving product details ex={}", e.getMessage());
            return new ResponseEntity<>(populateErrorMessage("An exception occured while saving product details: "+e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Product details updated successfully", HttpStatus.ACCEPTED);
    }

    public Error populateErrorMessage(String errorMessage) {
        Error error = new Error();
        error.setErrorMessage(errorMessage);
        return error;
    }

    @HystrixCommand(fallbackMethod = "productDetailsFallBack")
    public Map<String, Object> getProductInfo(String productId) {
        Map<String, Object> productInfo = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            HttpEntity<HttpHeaders> entity = new HttpEntity<>(httpHeaders);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> responseEntity = restTemplate.exchange(productUrl, HttpMethod.GET, entity, Map.class, productId);
            if (responseEntity != null) {
                productInfo = responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("An exception occured while fetching prodcutdetails ={}", e.getMessage());
            productInfo.put("error", populateErrorMessage("Product details not found for the id:" + productId));
            return productInfo;
        }
        return productInfo;
    }

    public Map<String, Object> productDetailsFallBack() {
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("errorMessage", "Product api is currently unavailable");
        return productInfo;
    }


    public Object populateProductDetails(Product productInfo, Map<String, Object> productMap) {
        com.target.retail.api.model.Product product = null;
        if (productInfo != null && productMap != null && productMap.get("product") != null) {
            product = new com.target.retail.api.model.Product();
            product.setId(String.valueOf(productInfo.getProductId()));
            if (((Map<String, Object>) productMap.get("product")).get("item") != null &&
                    ((Map<String, Object>) (((Map<String, Object>) productMap.get("product")).get("item"))).get("product_description") != null) {
                Map<String, Object> prodDescriptionMap = (Map<String, Object>) ((Map<String, Object>) (((Map<String, Object>) productMap.get("product")).get("item"))).get("product_description");
                String title = (String) prodDescriptionMap.get("title");
                product.setName(title);
            }
            Price price = new Price();
            price.setCurrencyCode("USD");
            price.setValue(Double.valueOf(productInfo.getPrice().toString()));
            product.setPrice(price);
        } else {
            log.error("label=populateProductDetails productInfo={} productMap={}", productInfo, productMap);
            return populateErrorMessage("Product Details are not available for the productId");
        }
        return product;
    }
}
