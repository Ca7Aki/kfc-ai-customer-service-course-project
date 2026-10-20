package org.example.mychatting.config;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChatMemoryStore implements ChatMemoryStore {

    private static final String STORAGE_DIR = "./chat_memories/";

    public FileChatMemoryStore() {
// 确保存储目录存在
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    @Override
    public List<ChatMessage>getMessages(Object memoryId) {
        System.out.println("从数据库中获取记忆信息...");
//编写查询数据库的语句....
        // 实际存储还是以mongodb、es等存储较为合适，这里为了方便，我们持久化到文件中即可
        Path filePath = getFilePath(memoryId);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            String json = new String(Files.readAllBytes(filePath));
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (IOException e) {
            System.err.println("Failed to read chat messages: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        if (list == null || list.isEmpty()) {
            deleteMessages(memoryId);
            return;
        }
//TODO 根据memoryId去数据库中获取修改、添加记录
        try {
            String json = ChatMessageSerializer.messagesToJson(list);
            Files.write(getFilePath(memoryId), json.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to save chat messages: " + e.getMessage());
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
//TODO 根据memoryId删除
        try {
            Files.deleteIfExists(getFilePath(memoryId));
        } catch (IOException e) {
            System.err.println("Failed to delete chat messages: " + e.getMessage());
        }
    }

    private Path getFilePath(Object memoryId) {
        return Paths.get(STORAGE_DIR + memoryId.toString() + ".json");
    }
}
