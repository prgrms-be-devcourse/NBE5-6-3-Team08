package com.grepp.datenow.infra.llm.config;

import com.mongodb.client.MongoClient;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import java.util.HashSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotPlaceConfig {

    // LangChain4j 에서 제공하는 로컬 실행 가능한 경량 임베딩 모델
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public MongoDbEmbeddingStore embeddingStore(EmbeddingModel embeddingModel,
        MongoClient mongoClient) {

        Boolean createIndex = true;
        IndexMapping indexMapping = IndexMapping.builder()
            .dimension(embeddingModel.dimension())
            .metadataFieldNames(new HashSet<>())
            .build();

        return MongoDbEmbeddingStore.builder()
            .databaseName("llm")
            .collectionName("places")
            .createIndex(createIndex)
            .indexName("vector_index")
            .indexMapping(indexMapping)
            .fromClient(mongoClient)
            .build();
    }

    @Bean
    EmbeddingStoreContentRetriever embeddingStoreContentRetriever(
        EmbeddingStore<TextSegment> embeddingStore,
        EmbeddingModel embeddingModel
    ){
        return EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(100)
            .minScore(0.6)
            .build();
    }
}
