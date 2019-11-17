package com.zyx.search;

import com.xuecheng.search.SearchApplication;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = SearchApplication.class)
@RunWith(SpringRunner.class)
public class TestCreate {
    @Autowired  //高等级
    private RestHighLevelClient restHighLevelClient;

    @Autowired  //低等级
    private RestClient restClient;

    @Test   //创建索引库
    public void createIndex() throws IOException {
        //创建索引对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
        //设置索引对象参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards","1").put("number_of_replicas","0"));
        //指定映射
        createIndexRequest.mapping("doc","{\n" +
                "\t\"properties\":{\n" +
                "\t\t\"name\":{\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\":\"ik_max_word\",\n" +
                "\t\t\t\"search_analyzer\":\"ik_smart\"\n" +
                "\t\t},\n" +
                "\t\t\"description\":{\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\":\"ik_max_word\",\n" +
                "\t\t\t\"search_analyzer\":\"ik_smart\"\n" +
                "\t\t},\n" +
                "\t\t\"pic\":{\n" +
                "\t\t\t\"type\":\"text\",\n" +
                "\t\t\t\"index\":false\n" +
                "\t\t},\n" +
                "\t\t\"price\":{\n" +
                "\t\t\t\"type\":\"float\"\n" +
                "\t\t},\n" +
                "\t\t\"studymodel\":{\n" +
                "\t\t\t\"type\": \"keyword\"\n" +
                "\t\t},\n" +
                "\t\t\"timestamp\":{\n" +
                "\t\t\t\"type\":\"date\",\n" +
                "\t\t\t\"format\":\"yyyy-MM-dd HH:mm:ss || yyyy-MM-dd || epoch_millis\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}", XContentType.JSON);
        //操作索引的客户端
        IndicesClient indices = restHighLevelClient.indices();
        //执行创建索引库
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest);
        //得到响应
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    @Test   //删除索引库
    public void deleteIndex() throws IOException {
        //创建删除索引对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
        //操作索引的客户端
        IndicesClient indices = restHighLevelClient.indices();
        //得到响应
        DeleteIndexResponse delete = indices.delete(deleteIndexRequest);
        //拿到响应
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);
    }

    @Test   //添加文档
    public void testAddDoc() throws IOException {
        //准备json数据
        Map<String,Object> jsonMap = new HashMap<>();
        jsonMap.put("name","spring cloud实战");
        jsonMap.put("description","本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud\n" +
                "基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel","201001");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMap.put("timestamp",dateFormat.format(new Date()));
        jsonMap.put("price",5.6f);
        //添加索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course","doc");
        //文档内容
        indexRequest.source(jsonMap);
        //通过client进行http的请求
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest);
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);
    }

    @Test   //查询文档
    public void testGetDoc() throws IOException {
        //查询请求对象
        GetRequest getRequest = new GetRequest("xc_course","doc","Je5xEG4BeYjjhS8G6nIG");
        GetResponse getResponse = restHighLevelClient.get(getRequest);
        //文档内容
        Map<String, Object> source = getResponse.getSourceAsMap();
        System.out.println(source);
    }

    @Test   //更新文档
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_course","doc","Je5xEG4BeYjjhS8G6nIG");
        Map<String,Object> map = new HashMap<>();
        map.put("name","springCloud实战");
        updateRequest.doc(map);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest);
        RestStatus status = updateResponse.status();
        System.out.println(status);
    }

    @Test   //根据id删除文档
    public void testDeleteDoc() throws IOException {
        String id = "Je5xEG4BeYjjhS8G6nIG";
        DeleteRequest deleteRequest = new DeleteRequest("xc_course","doc",id);
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest);
        DocWriteResponse.Result result = deleteResponse.getResult();
        System.out.println(result);
    }
}
