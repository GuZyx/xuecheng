package com.zyx.search;

import com.xuecheng.search.SearchApplication;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@SpringBootTest(classes = SearchApplication.class)
@RunWith(SpringRunner.class)
public class TestSearch {
    @Autowired  //高等级
    private RestHighLevelClient restHighLevelClient;

    @Autowired  //低等级
    private RestClient restClient;

    @Test //搜索全部记录
    public void searchAll() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方法
        //matchAll搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                        "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发送http发送请求
        this.result(searchRequest);
    }

    @Test //搜索全部记录，分页
    public void searchPageAll() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        //第几页
        //页码
        int page = 1;
        //每页记录数
        int size = 1;
        //计算出来记录的起始下标
        int from = (page-1)*size;
        searchSourceBuilder.from(from); //起始下标，从0开始
        searchSourceBuilder.size(size); //每页显示的记录数
        //搜索方法
        //matchAll搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发送http发送请求
        this.result(searchRequest);
    }

    @Test //精确查询，在搜索时会整体匹配关键字，不再将关键字分词。
    public void termQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        //第几页
        //页码
        int page = 1;
        //每页记录数
        int size = 1;
        //计算出来记录的起始下标
        int from = (page-1)*size;
        searchSourceBuilder.from(from); //起始下标，从0开始
        searchSourceBuilder.size(size); //每页显示的记录数
        //搜索方法
        //termQuery 精确查询
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        this.result(searchRequest);
    }

    @Test //根据id精确匹配
    public void termQueryByIds() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方法
        //termQuery 精确查询(根据id)
        //定义id
        String ids[] = new String[]{"1","3","6"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        this.result(searchRequest);
    }

    @Test //即全文检索，它的搜索方式是先将搜索字符串分词，再使用各各词条从索引中搜索
    public void matchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方法
        //matchQuery 先将搜索字符串分词，再使用各各词条从索引中搜索
        searchSourceBuilder.query(QueryBuilders.matchQuery("description","spring开发框架")
                .minimumShouldMatch("80%"));
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        this.result(searchRequest);
    }

    @Test //一次可以匹配多个字段，提升字段权重
    public void multiMatchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方法
        //termQuery 精确查询(根据id)
        //定义id
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css","name","description")
        .minimumShouldMatch("50%")
        .field("name",10)); //提升字段的 boost（权重）来提高得分
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        this.result(searchRequest);
    }

    @Test //布尔查询对应于Lucene的BooleanQuery查询，实现将多个查询组合起来。
    public void boolQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方法
        //termQuery 精确查询(根据id)
        //定义id
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);//提升字段的 boost（权重）来提高得分
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(matchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        this.result(searchRequest);
    }

    @Test //过虑是针对搜索的结果进行过虑，过虑器主要判断的是文档是否匹配，不去计算和判断文档的匹配度得分，所以过
    //虑器性能比查询要高，且方便缓存，推荐尽量使用过虑器去实现查询或者过虑器和查询共同使用。。
    public void filterQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方法
        //termQuery 精确查询(根据id)
        //定义id
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);//提升字段的 boost（权重）来提高得分
        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));
        boolQueryBuilder.must(matchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        this.result(searchRequest);
    }

    @Test //可以在字段上添加一个或多个排序,text类型的字段上不允许添加排序。
    public void sortQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);
        //添加排序，在searchSourceBuilder中    Desc降序，ASC升序
        searchSourceBuilder.sort("price", SortOrder.ASC);
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        this.result(searchRequest);
    }

    @Test //高亮显示可以将搜索结果一个或多个字突出显示，以便向用户展示匹配关键字的位置
    public void highLightQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方法
        //termQuery 精确查询(根据id)
        //定义id
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);//提升字段的 boost（权重）来提高得分
        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        boolQueryBuilder.must(matchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段的过滤，第一个参数结果集包含哪些字段，第二个参数结果集不包含哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel",
                "price","timestamp"}, new String[]{});
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");
        highlightBuilder.postTags("</tag>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
//        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        //将highLightBuilder放入searchSourceBuilder
        searchSourceBuilder.highlighter(highlightBuilder);
        //向搜索请求对象中指定搜索源对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发送http发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索的结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(SearchHit searchHit:searchHits){
            System.out.println("文档的主键"+searchHit.getId());
            //源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //##源文档的name字段内容
            String name = (String) sourceAsMap.get("name");
            //##取出name的高亮字段
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if(highlightFields!=null){
                StringBuffer stringBuffer = new StringBuffer();
                HighlightField highName = highlightFields.get("name");
                if(highName!=null){
                    //取出name高亮字段
                    Text[] fragments = highName.getFragments();
                    for(Text text:fragments){
                        stringBuffer.append(text);
                    }
                }
                name = stringBuffer.toString();
            }
            //由于设置了源文档字段的过滤，这时description是取不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = simpleDateFormat.parse((String)sourceAsMap.get("timestamp"));
        }
    }

    //执行搜索及得出的结果
    private void result(SearchRequest searchRequest) throws ParseException, IOException {
        //执行搜索，向ES发送http发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索的结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(SearchHit searchHit:searchHits){
            System.out.println("文档的主键"+searchHit.getId());
            //源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于设置了源文档字段的过滤，这时description是取不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = simpleDateFormat.parse((String)sourceAsMap.get("timestamp"));
        }
    }
}
