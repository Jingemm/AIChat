package com.aichat.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.aichat.entity.Document;
import com.aichat.util.EmbeddingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class EsDocumentService {
    @Autowired
    private ElasticsearchClient esClient;
    @Autowired
    private EmbeddingUtil embeddingUtil;

    private static final String INDEX_NAME = "doc_index";

    public void indexDocument(Document doc) throws IOException {
        List<Double> embeddingDouble = embeddingUtil.embed(doc.getContent());
        List<Float> embedding = new ArrayList<>();
        for (Double d : embeddingDouble) {
            embedding.add(d.floatValue());
        }

        Map<String, Object> docMap = new HashMap<>();
        docMap.put("id", doc.getId().toString());
        docMap.put("title", doc.getTitle());
        docMap.put("content", doc.getContent());
        docMap.put("embedding", embedding);

        IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                .index(INDEX_NAME)
                .id(doc.getId().toString())
                .document(docMap));
        esClient.index(request);
    }

    public List<Map<String, Object>> hybridSearch(String query, int topK) throws IOException {
        // 1. 关键词搜索
        SearchResponse<Map> keywordResponse = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.match(m -> m.field("content").query(query)))
                .size(topK), Map.class);

        // 2. 向量搜索
        List<Double> queryVecDouble = embeddingUtil.embed(query);
        List<Float> queryVec = new ArrayList<>();
        for (Double d : queryVecDouble) {
            queryVec.add(d.floatValue());
        }
        SearchResponse<Map> vectorResponse = esClient.search(s -> s
                .index(INDEX_NAME)
                .knn(knn -> knn
                        .field("embedding")
                        .queryVector(queryVec)
                        .k(topK)
                        .numCandidates(topK * 10)
                ), Map.class);

        // 3. 合并得分
        Map<String, Double> scoreMap = new LinkedHashMap<>();
        for (Hit<Map> hit : keywordResponse.hits().hits()) {
            scoreMap.merge(hit.id(), hit.score() != null ? hit.score() : 0.0, Double::sum);
        }
        for (Hit<Map> hit : vectorResponse.hits().hits()) {
            scoreMap.merge(hit.id(), hit.score() != null ? hit.score() : 0.0, Double::sum);
        }

        // 4. 按得分降序排序，取出前 topK 个 ID
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(scoreMap.entrySet());
        sortedEntries.sort(new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // 5. 取前 topK 个，手动构造结果列表
        List<Map<String, Object>> resultList = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Double> entry : sortedEntries) {
            if (count >= topK) break;
            String targetId = entry.getKey();
            boolean found = false;
            for (Hit<Map> hit : keywordResponse.hits().hits()) {
                if (hit.id().equals(targetId)) {
                    resultList.add(hit.source());
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (Hit<Map> hit : vectorResponse.hits().hits()) {
                    if (hit.id().equals(targetId)) {
                        resultList.add(hit.source());
                        break;
                    }
                }
            }
            count++;
        }
        return resultList;
    }
}