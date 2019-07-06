package com.target.retail.api.dao;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.target.retail.api.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RetailDao {

    @Autowired
    CassandraTemplate cassandraTemplate;

    public void updateProductDetails(Product product) {
        if(cassandraTemplate != null) {
            cassandraTemplate.update(product);
        }
    }


    public Product getProductPrice(long productId) {
        Product product = null;
        if(cassandraTemplate != null) {
            Select select = QueryBuilder.select().from("product_details").where(QueryBuilder.eq("product_id",productId)).allowFiltering();
            product = cassandraTemplate.selectOne(select,Product.class);
        }
        return product;
    }

}
