package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES搜索业务层
 */
@Service
public class ESCourseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESCourseService.class);

    @Value("${xuecheng.course.index}")
    private String es_index;

    @Value("${xuecheng.media.index}")
    private String media_index;

    @Value("${xuecheng.course.type}")
    private String type;

    @Value("${xuecheng.media.type}")
    private String media_type;

    @Value("${xuecheng.course.source_field}")
    private String source_field;

    @Value("${xuecheng.media.source_field}")
    private String media_source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    //搜索课程信息
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if(courseSearchParam==null){
            courseSearchParam = new CourseSearchParam();
        }
        //创建搜索的请求对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        searchRequest.types(type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //指定过滤源字段
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields,new String[]{});
        //分页
        if(page<=0){
            page=1;
        }
        if(size<=0){
            size=12;
        }
        int from = (page-1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //搜索条件
        //根据关键字搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            //匹配关键字
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery
                    (courseSearchParam.getKeyword(), "name", "teachplan", "description")
                    .minimumShouldMatch("70%")  //设置匹配占比
                    .field("name",10); //提升另个字段的Boost值
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //过滤
        //根据一级分类查询
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        //根据二级分类查询
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        //根据难度等级查询
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }
        //价格区间
        if(courseSearchParam.getPrice_max()!=null && courseSearchParam.getPrice_min()!=null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(courseSearchParam.getPrice_min()).lte(courseSearchParam.getPrice_max()));
        }
        //设置boolQueryBuilder到searchSourceBuilder
        searchSourceBuilder.query(boolQueryBuilder);

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> coursePubs = new ArrayList<>();
        try {
            //执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits searchHits = searchResponse.getHits();
            //匹配的总记录数
            long totalHits = searchHits.totalHits;
            queryResult.setTotal(totalHits);
            SearchHit[] searchList = searchHits.getHits();
            for(SearchHit searchHit:searchList){
                CoursePub coursePub = new CoursePub();
                //源文档
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //取出name
                String name = (String) sourceAsMap.get("name");
                //取出高亮字段内容
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                if(highlightFields!=null){
                    HighlightField nameField = highlightFields.get("name");
                    if(nameField!=null){
                        Text[] texts = nameField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for(Text text:texts){
                            stringBuffer.append(text.string());
                        }
                        name=stringBuffer.toString();
                    }
                }
                coursePub.setName(name);

                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try
                    {
                        if(sourceAsMap.get("price")!=null ){
                            price = (Double) sourceAsMap.get("price");
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                coursePub.setPrice(price);

                Double price_old = null;
                try {
                    if(sourceAsMap.get("price_old")!=null ){
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                coursePubs.add(coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("xuecheng search error..{}",e.getMessage());
        }
        queryResult.setList(coursePubs);
        return new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
    }

    //使用ES客户端向ES请求查询索引信息
    public Map<String, CoursePub> getall(String id) {
        //定义一个搜索的请求对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        //定义type
        searchRequest.types(type);
        //定义searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termquery
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        //过滤源字段
        //searchSourceBuilder.fetchSource()
        searchRequest.source(searchSourceBuilder);
        //最终要返回的课程信息
        Map<String,CoursePub> map = new HashMap<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for(SearchHit searchHit:searchHits){
                //获取源文档的内容
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //课程id
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");

                CoursePub coursePub = new CoursePub();
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                coursePub.setCharge(charge);
                map.put(courseId,coursePub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    //根据多个课程计划id查询课程媒资信息
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //定义一个搜索的请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //定义type
        searchRequest.types(media_type);
        //定义searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //过滤源字段
        String[] split = media_source_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});

        //设置使用termquery,根据多个id查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        searchRequest.source(searchSourceBuilder);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long totalHits = 0;
        try {
            //执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            totalHits = hits.totalHits;
            for(SearchHit searchHit:searchHits){
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //取出课程计划媒资信息
                String teachplanId = (String) sourceAsMap.get("teachplan_id");
                String mediaId = (String) sourceAsMap.get("media_id");
                String mediaFileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
                String mediaUrl = (String) sourceAsMap.get("media_url");
                String courseid = (String) sourceAsMap.get("courseid");
                teachplanMediaPub.setTeachplanId(teachplanId);
                teachplanMediaPub.setMediaId(mediaId);
                teachplanMediaPub.setMediaFileOriginalName(mediaFileoriginalname);
                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(mediaUrl);
                teachplanMediaPubList.add(teachplanMediaPub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setTotal(totalHits);
        queryResult.setList(teachplanMediaPubList);
        return new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
    }
}
