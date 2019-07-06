package com.target.retail.api.controller;

import com.target.retail.api.model.Error;
import com.target.retail.api.model.Product;
import com.target.retail.api.service.RetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping(value = "/retail-api", produces = "application/json")
public class RetailController {

    @Autowired
    RetailService retailService;

    @RequestMapping(value = "/products/{id}",method = RequestMethod.GET)
    public ResponseEntity<Object> getProduct(@PathVariable(required = true, name = "id") String id) {
        log.info("label=RetailController getProduct started");
        return retailService.getProductDetails(id);
    }

    @RequestMapping(value = "/products/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Object> updateProduct(@PathVariable(required = true, name = "id") String id, @RequestBody Product product)throws Exception {
        log.info("label=RetailController updateProduct started");
        return retailService.updateProductDetails(id,product);
    }


}
