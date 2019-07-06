package com.target.retail.api

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session
import com.target.retail.api.controller.RetailController
import com.target.retail.api.dao.RetailDao
import com.target.retail.api.model.Error
import com.target.retail.api.model.Price
import com.target.retail.api.model.Product
import com.target.retail.api.service.RetailService
import com.target.retail.api.service.RetailServiceImpl
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.springframework.data.cassandra.core.CassandraTemplate
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification


class RetailApiSpec extends Specification {

    @Shared
    RetailController controller;
    @Shared
    RetailDao retailDao

    @Shared
    RetailService retailService

    def setupSpec() {


        EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE, 1000000L);
        Cluster cluster = Cluster.builder().withoutJMXReporting().withClusterName("Test Cluster").addContactPoints("127.0.0.1").withPort(9142).build();

        Session session = cluster.connect()
        session.execute("CREATE KEYSPACE product WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'};");
        session.close();
        session = cluster.connect("product")
        session.execute("CREATE TABLE PRODUCT_DETAILS(PRICE DECIMAL,PRODUCT_ID BIGINT PRIMARY KEY)")
        session.execute("INSERT INTO PRODUCT_DETAILS(PRICE,PRODUCT_ID) VALUES (12.99,13860428)")
        session.execute("INSERT INTO PRODUCT_DETAILS(PRICE,PRODUCT_ID) VALUES (12.99,13860429)")
        session.execute("INSERT INTO PRODUCT_DETAILS(PRICE,PRODUCT_ID) VALUES (12.99,1386042)")
        CassandraTemplate cassandraTemplate = new CassandraTemplate(session);

        controller = new RetailController()
        retailDao = new RetailDao()
        retailService = new RetailServiceImpl()
        retailService.productUrl = "https://redsky.target.com/v2/pdp/tcin/{productId}?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics"


        retailDao.cassandraTemplate = cassandraTemplate;
        retailService.retailDao = retailDao;

        controller.retailService = retailService;
    }

    def cleanup() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    def 'Test get products with valid productId'() {

        given:
        Map<String,Object> productDescriptionMap = new HashMap<>();
        productDescriptionMap.put("title","SpongeBob SquarePants: SpongeBob's Frozen Face-off")

        Map<String,Object> itemMap = new HashMap<>();
        itemMap.put("product_description",productDescriptionMap)

        Map<String,Object> productMap = new HashMap<>();
        productMap.put("item",itemMap)

        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("product",productMap)

        _ * retailService.getProductInfo("13860429") >> responseMap

        when:
        ResponseEntity<Object> product = controller.getProduct("13860429");

        then:
        product != null
        product.body.id == '13860429'
        product.body.name == 'SpongeBob SquarePants: SpongeBob\'s Frozen Face-off'
        product.body.price != null
        product.body.price.value == 12.99
        product.body.price.currencyCode == 'USD'

    }

    def 'Test get products with invalid productId'() {

        given:
        _ * retailService.getProductInfo("1386042") >> new HashMap<>();

        when:
        ResponseEntity<Object> product = controller.getProduct("1386042");

        then:
        product != null
        product.body instanceof Error
        product.body.errorMessage != null
        product.body.errorMessage == 'Product Details are not available for the productId'
        println(product.body.errorMessage)

    }

    def 'Update price details'() {
        given:
        Product product = new Product();
        product.setId("13860429")
        product.setName("The Big Lebowski (Blu-ray)")
        Price price = new Price();
        price.setCurrencyCode("USD")
        price.setValue(12.11)
        product.setPrice(price)

        when:
        ResponseEntity<Object> object = controller.updateProduct("13860429", product)

        then:
        object != null
    }

    def 'Update price details throws exception'() {
        given:
        Product product = new Product();
        product.setId("13860429")
        product.setName("The Big Lebowski (Blu-ray)")
        Price price = new Price();
        price.setCurrencyCode("USD")
        price.setValue(12.11)
        product.setPrice(price)
        _ * retailDao.getProductPrice(1386042) >> null;

        when:
        ResponseEntity<Object> object = controller.updateProduct("1386042", product)

        then:
        object != null
    }

}
