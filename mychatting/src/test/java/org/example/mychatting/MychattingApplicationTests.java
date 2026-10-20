package org.example.mychatting;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.example.mychatting.config.AiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@SpringBootTest
class MychattingApplicationTests {

    @Autowired
    ChatLanguageModel chatLanguageModel;

    @Test
    void contextLoads() {
        String chat = chatLanguageModel.chat("我爱的人他不爱我怎么办");
        System.out.println(chat);
    }

    @Test
    void contextLoads2() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-chat")
                .build();
        String chat = model.chat("怎么让别人爱上我");
        System.out.println(chat);
    }

    @Test
    void test3() {
        String prompt = "一只在阳光下草地上奔跑的金毛犬，卡通风格";

        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .model("wanx2.1-t2i-turbo")
                        .prompt(prompt)
                        .n(1)
                        .size("1024*1024")
                        .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();//类名叫图像合成
        ImageSynthesisResult result = null;
        try {
            System.out.println("---sync call, please wait a moment----");
            result = imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e){
            throw new RuntimeException(e.getMessage());
        }
        System.out.println(JsonUtils.toJson(result));
    }

    @Autowired
    StreamingChatLanguageModel streamingChatLanguageModel;

    @Test
    void test4() throws InterruptedException {

        Flux flux=
                Flux.create(sink -> {
// 将来执行的时候，AI每产生一个三五个字的响应就会把这个匿名函数执行一遍
                    streamingChatLanguageModel.chat("我是谁", new StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String partialResponse) {
//                 System.out.println("onPartialResponse: " + partialResponse);
// 将部分响应发送到流
                            sink.next(partialResponse);
                        }
                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
// 本次会话传输结束后会执行
//                 System.out.println("onCompleteResponse: " + completeResponse);
// 标记流完成
                            sink.complete();
                        }
                        @Override
                        public void onError(Throwable error) {
                            error.printStackTrace();
// 将错误发送到流
                            sink.error(error);
                        }
                    });
                }, FluxSink.OverflowStrategy.BUFFER);

        //上面只是流的定义，并没有真的问AI，AI当然也不会真的返回数据并且被sink发送到流，是下面这一个语句真的产生了访问AI的行为
//这是在java里，这个代码成为订阅者，如果是在WEB，spring框架成为订阅者，把流的输出值一点一滴送给网页，在网页上显示出来
        System.out.println("22222222222");
        Thread.sleep(8000);
        flux.subscribe(System.out::println);
        System.out.println("11111111111111");
        Thread.sleep(2000);
        System.out.println("0000000000000");

    }

    @Autowired
    AiConfig.Assistant assistant;

    @Test
    void contextLoads3() throws InterruptedException {
        String chat1 = assistant.chat("1","我叫郭小芙芙");
        System.out.println(chat1);
        Thread.sleep(10000);
        System.out.println("****************************");
        chat1 = assistant.chat("3","我是谁");
        System.out.println(chat1);
        Thread.sleep(10000);
        System.out.println("########################");
        chat1 = assistant.chat("1","我是谁");
        System.out.println(chat1);

    }

    @Test
    public void test31() {
        // ------------------------------------------ 向量嵌入（Embedding）阶段 ------------------------------------------
        // 创建一个内存中的向量存储，用于存储文本片段及其对应的向量
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        // 使用建造者模式配置模型参数
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .build(); // 完成模型构建
        // 存入向量数据库的第一个文本片段（关于航班预订的条款）
        TextSegment segment1 = TextSegment.from("""
 预订酒店:
- 通过我们的官方网站、移动应用程序或授权合作伙伴平台完成预订。
- 预订时需要全额付款。
- 请确保入住人信息（姓名、联系方式、入住日期、房型等）准确。
""");
        // 将文本片段转换为向量并存储到向量数据库
        embeddingStore.add(embeddingModel.embed(segment1).content(), segment1);
        // 存入向量数据库的第二个文本片段（关于取消预订的条款）
        TextSegment segment2 = TextSegment.from("""
 取消预订:
- 最晚在入住日前 6 小时 取消以避免罚金。
 - 取消费用：经济房型：首晚房费的60%，豪华房型：首晚房费的40%，套房/别墅：首晚房费
的20%。
 - 退款将在 5-10个工作日 原路退回。
 """);
        // 将文本片段转换为向量并存储到向量数据库
        embeddingStore.add(embeddingModel.embed(segment2).content(), segment2);
        // ---------------------- 数据检索阶段 ----------------------
        // 将用户的查询语句"取消航班要多少钱"转换为向量
        Embedding queryEmbedding = embeddingModel.embed("取消预订要多少钱").content();
        // 构建向量搜索请求
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding) // 设置查询向量
                .maxResults(1) // 设置最大返回结果数为1
                .minScore(0.6) // 设置最小相似度阈值为0.6
                .build();
        // 执行向量搜索
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        // 处理搜索结果
        if (result.matches().isEmpty()) {
            // 如果没有找到匹配结果
            System.out.println("未找到匹配结果！");
        } else {
            // 遍历所有匹配结果（这里由于maxResults=1，只会有一个结果）
            result.matches().forEach(match -> {
                System.out.println("相似度: " + match.score()); // 打印匹配相似度分数
                System.out.println("匹配内容: " + match.embedded().text()); // 打印匹配的文本内容
            });
        }
    }


}
