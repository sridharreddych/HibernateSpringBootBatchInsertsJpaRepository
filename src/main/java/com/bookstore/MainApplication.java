package com.bookstore;

import com.bookstore.service.BookstoreService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainApplication {

    private final BookstoreService bookstoreService;

    public MainApplication(BookstoreService bookstoreService) {
        this.bookstoreService = bookstoreService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public ApplicationRunner init() {
        return args -> {
            bookstoreService.batchAuthors();
        };
    }
}

/*
 * Batch Inserts via saveAll(Iterable<S> entities) in MySQL

Description: Batch inserts via SimpleJpaRepository#saveAll(Iterable<S> entities) method in MySQL

Key points:

in application.properties set spring.jpa.properties.hibernate.jdbc.batch_size
in application.properties set spring.jpa.properties.hibernate.generate_statistics (just to check that batching is working)
in application.properties set JDBC URL with rewriteBatchedStatements=true (optimization for MySQL)
in application.properties set JDBC URL with cachePrepStmts=true (enable caching and is useful if you decide to set prepStmtCacheSize, prepStmtCacheSqlLimit, etc as well; without this setting the cache is disabled)
in application.properties set JDBC URL with useServerPrepStmts=true (this way you switch to server-side prepared statements (may lead to signnificant performance boost))
in case of using a parent-child relationship with cascade persist (e.g. one-to-many, many-to-many) then consider to set up spring.jpa.properties.hibernate.order_inserts=true to optimize the batching by ordering inserts
in entity, use the assigned generator since MySQL IDENTITY will cause insert batching to be disabled
in entity, add @Version property to avoid extra-SELECT statements fired before batching (also prevent lost updates in multi-request transactions). Extra-SELECT statements are the effect of using merge() instead of persist(); behind the scene, saveAll() uses save(), which in case of non-new entities (entities that have IDs) will call merge(), which instruct Hibernate to fire a SELECT statement to make sure that there is no record in the database having the same identifier
pay attention on the amount of inserts passed to saveAll() to not "overwhelm" the Persistence Context; normally the EntityManager should be flushed and cleared from time to time, but during the saveAll() execution you simply cannot do that, so if in saveAll() there is a list with a high amount of data, all that data will hit the Persistence Context (1st Level Cache) and will remain in memory until the flush time; using relatively small amount of data should be ok (in this example, each batch of 30 entities run in a separate transaction and Persistent Context)
the saveAll() method return a List<S> containing the persisted entities; each persisted entity is added into this list; if you just don't need this List then it is created for nothing
if is not needed, then ensure that Second Level Cache is disabled via spring.jpa.properties.hibernate.cache.use_second_level_cache=false
 */
