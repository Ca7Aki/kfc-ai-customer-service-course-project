package org.example.mychatting.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    @Tool("广州有多少个名字的")
    public Integer guangZhouNameCount(@P("姓名") String name){
        System.out.println(name);
        //  去业务逻辑 数据库 或者系统中查询这个人，这里直接返回10000，实际是一种假设，假设从数据库或者文献中搜索计算出的结果是10000个，也就是说这里省略了一大段搜索文档或者访问数据库的代码。
        return 10000;
    }

}
