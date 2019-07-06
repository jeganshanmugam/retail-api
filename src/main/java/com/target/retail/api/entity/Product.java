package com.target.retail.api.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Table(value="PRODUCT_DETAILS")
public class Product implements Serializable {

    @Column(value = "PRICE")
    private BigDecimal price;

    @PrimaryKey(value = "PRODUCT_ID")
    private long productId;

}
