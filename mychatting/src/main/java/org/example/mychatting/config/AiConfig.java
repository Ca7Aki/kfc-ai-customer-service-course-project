package org.example.mychatting.config;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.util.List;

@Configuration
public class AiConfig {

    // 阿里云百炼 DashScope API Key，从配置项 dashscope.api-key 读取（环境变量 DASHSCOPE_API_KEY）
    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    // 1. 注册 FileChatMemoryStore
    @Bean
    public ChatMemoryStore fileChatMemoryStore() {
        return new FileChatMemoryStore();
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }

    @Bean
    public QwenEmbeddingModel qwenEmbeddingModel() {
        // 使用建造者模式配置模型参数（api-key 来自配置注入，不硬编码）
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey(dashScopeApiKey)
                .build();  // 完成模型构建
        return embeddingModel;
    }

    // 定义AI助手的公共接口
    public interface Assistant{
        // 普通聊天方法（阻塞式，返回完整响应）带聊天记忆功能
        @SystemMessage("""
您是 “肯德基 KFC” 的在线售后客服助手，将以专业、友好且耐心的方式为用户提供帮助。
您正在通过在线聊天系统与客户实时互动。
在处理退款、补偿以及售后申诉前，请务必确认以下信息：

1. KFC 订单编号
2. 下单时间
3. 问题实拍照片或视频举证

今日日期：{{current_date}}
服务时间：7×24 小时（3 分钟内响应）

请特别注意：

- 售后有效时效为订单完成后 24 小时，超时仅可提交人工复核，无法保证赔付
- KFC 餐品一经出餐交付，不提供补发，仅支持退款或优惠券补偿
- 同一订单仅可受理一次有效售后，重复申诉、虚假举证不予处理
- 始终优先使用礼貌用语："您"" 请 ""感谢"
- 回答严格依据 RAG 知识库，知识库不存在的政策不要自行编造，转交人工专员处理

当前可提供服务：
✓漏送 / 错送 / 缺料售后受理
✓餐品洒漏、破损、异物变质售后申请
✓配送超时补偿咨询
✓退款进度查询、退款未到账问题处理
✓电子发票申请咨询
✓门店服务问题登记投诉

不可提供服务：
✗无法处理非 KFC 订单售后
✗不受理单纯口味不满意、仅口感变冷变软类无质量问题退款诉求
✗不支持已出餐订单无理由取消退款
                       """)
        String chat(@MemoryId String memoryId, @UserMessage String message);

        // 流式响应方法（实时返回生成的token）带聊天记忆功能
        @SystemMessage("""
您是 “肯德基 KFC” 的在线售后客服助手，将以专业、友好且耐心的方式为用户提供帮助。
您正在通过在线聊天系统与客户实时互动。
在处理退款、补偿以及售后申诉前，请务必确认以下信息：

1. KFC 订单编号
2. 下单时间
3. 问题实拍照片或视频举证

今日日期：{{current_date}}
服务时间：7×24 小时（3 分钟内响应）

请特别注意：

- 售后有效时效为订单完成后 24 小时，超时仅可提交人工复核，无法保证赔付
- KFC 餐品一经出餐交付，不提供补发，仅支持退款或优惠券补偿
- 同一订单仅可受理一次有效售后，重复申诉、虚假举证不予处理
- 始终优先使用礼貌用语："您"" 请 ""感谢"
- 回答严格依据 RAG 知识库，知识库不存在的政策不要自行编造，转交人工专员处理

当前可提供服务：
✓漏送 / 错送 / 缺料售后受理
✓餐品洒漏、破损、异物变质售后申请
✓配送超时补偿咨询
✓退款进度查询、退款未到账问题处理
✓电子发票申请咨询
✓门店服务问题登记投诉

不可提供服务：
✗无法处理非 KFC 订单售后
✗不受理单纯口味不满意、仅口感变冷变软类无质量问题退款诉求
✗不支持已出餐订单无理由取消退款
                       """)
        TokenStream stream(@MemoryId String memoryId, @UserMessage String message);
    }

    // 定义Spring Bean来创建助手实例
    @Bean  // 标记为Spring管理的Bean
    public Assistant getAssistant(// 注入标准聊天语言模型（用于普通响应）
                                  ChatLanguageModel chatLanguageModel,
                                  // 注入自定义的聊天记录持久化方案
                                  ChatMemoryStore chatMemoryStore,
                                  // 注入流式聊天语言模型（用于流式响应）
                                  StreamingChatLanguageModel streamingChatLanguageModel,
                                  EmbeddingStore embeddingStore,
                                  QwenEmbeddingModel qwenEmbeddingModel){

        // 内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5) // 最相似的5个结果
                .minScore(0.6) // 只找相似度在0.6以上的内容
                .build();

        // 使用AI服务构建助手实现
        Assistant assistant = AiServices.builder(Assistant.class)
                // 设置标准聊天模型（用于chat()方法）
                .chatLanguageModel(chatLanguageModel)
                // 设置流式聊天模型（用于stream()方法）
                .streamingChatLanguageModel(streamingChatLanguageModel)
                // 配置对话记忆提供者（为每个memoryId创建独立的记忆窗口）
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .maxMessages(10)
                                .id(memoryId)
                                // 使用自定义的持久化方案
                                .chatMemoryStore(chatMemoryStore)
                                .build()
                )
                .contentRetriever(contentRetriever)
                // 完成构建
                .build();
        // 返回配置好的助手实例
        return assistant;
    }


    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(QwenEmbeddingModel qwenEmbeddingModel, EmbeddingStore embeddingStore)
            throws URISyntaxException {
        Document document = ClassPathDocumentLoader.loadDocument("rag/rag-service.txt",new TextDocumentParser());
        return args->{
            DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(200, 50);
            List<TextSegment> segments = splitter.split(document);
            List<Embedding> embeddings = qwenEmbeddingModel.embedAll(segments).content();
            embeddingStore.addAll(embeddings,segments);
        };

    }
}