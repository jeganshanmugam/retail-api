package com.target.retail.api.service;

import com.target.retail.api.model.Product;
import org.springframework.http.ResponseEntity;

import javax.xml.ws.Response;

public interface RetailService {

    public ResponseEntity<Object> getProductDetails(String id);

    public ResponseEntity<Object> updateProductDetails(String id, Product product) throws Exception;


}
