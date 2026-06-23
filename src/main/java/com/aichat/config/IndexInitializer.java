package com.aichat.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexInitializer {
    @Autowired
    private ElasticsearchClient esClient;

    @PostConstruct
    public void init() throws Exception {
        String indexName = "doc_index";
        ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
        BooleanResponse exists = esClient.indices().exists(existsRequest);
        if (!exists.value()) {
            CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
                .index(indexName)
                .mappings(mapping -> mapping
                    .properties("id", Property.of(p -> p.keyword(k -> k)))
                    .properties("title", Property.of(p -> p.text(t -> t.analyzer("standard"))))
                    .properties("content", Property.of(p -> p.text(t -> t.analyzer("standard"))))
                    .properties("embedding", Property.of(p -> p
                        .denseVector(dv -> dv.dims(1024).similarity("cosine"))
                    ))
                )
            );
            esClient.indices().create(request);
            System.out.println("ES 索引创建成功");
        }
    }
}
