package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * cms_page 集合业务层
 */
@Service
public class CmsPageService {
    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询列表
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        if(queryPageRequest==null){ //判断传进来的queryPageRequest是否为空
            queryPageRequest = new QueryPageRequest();
        }

        //自定义条件查询
        //  条件值对象   cmsPage
        CmsPage cmsPage = new CmsPage();
        //设置条件值
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){//站点id
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){//模板id
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){//别名(模糊查询)
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        //  条件匹配器    matcher
        ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("pageAliase"
                ,ExampleMatcher.GenericPropertyMatchers.contains());

        //定义Example     .of(条件值对象，条件匹配器)
        Example<CmsPage> example = Example.of(cmsPage,matcher);

        if(page <= 0){ page = 1; }
        if(size <= 0){ size = 10; }

        Pageable pageable = PageRequest.of(page-1,size);
        //实现自定义条件查询及分页查询
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example,pageable);

        QueryResult<CmsPage> result = new QueryResult<>();  //
        result.setList(cmsPages.getContent());
        result.setTotal(cmsPages.getTotalElements());

        return new QueryResponseResult(CommonCode.SUCCESS,result);  //调用QueryResponseResult构造方法
    }

    /**
     * 添加操作
     * @param cmsPage
     * @return
     */
   /* public CmsPageResult add(CmsPage cmsPage){
        //校验页面名称，站点id，页面webpath的唯一性
        //根据页面名称，站点id，页面webpath去cms_page查询，如果查到说明已存在，查询不到继续添加

        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(null!=cmsPage1){ //不唯一，不能添加因为唯一性
            return new CmsPageResult(CommonCode.FAIL,null);
        }
        //调动dao新增页面
        cmsPage.setPageId(null);  //防止主键被别人创建，让MongoDB自动生成
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }*/
    public CmsPageResult add(CmsPage cmsPage){
        if(cmsPage==null){
            //抛出异常，非法参数异常..
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //校验页面名称，站点id，页面webpath的唯一性
        //根据页面名称，站点id，页面webpath去cms_page查询，如果查到说明已存在，查询不到继续添加
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(null!=cmsPage1){ //页面已存在
            //抛出异常，异常内容就是页面已存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //调动dao新增页面
        cmsPage.setPageId(null);  //防止主键被别人创建，让MongoDB自动生成
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }

    /**
     * 根据id查询对象
     * @param pageId
     * @return
     */
    public CmsPage findById(String pageId){
        Optional<CmsPage> optional= cmsPageRepository.findById(pageId);
        return optional.orElse(null);   // cmsPage != null ? cmsPage : null
    }

    /**
     * 根据id修改对象
     * @param pageId
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String pageId,CmsPage cmsPage){
        //根据id从数据库查询信息
        CmsPage cmsChange= this.findById(pageId);
        if(cmsChange == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        // 更新模板站点
        cmsChange.setTemplateId(cmsPage.getTemplateId());
        // 更新所属站点
        cmsChange.setSiteId(cmsPage.getSiteId());
        // 更新页面别名
        cmsChange.setPageAliase(cmsPage.getPageAliase());
        // 更新页面名称
        cmsChange.setPageName(cmsPage.getPageName());
        // 更新访问路径
        cmsChange.setPageWebPath(cmsPage.getPageWebPath());
        // 更新物理路径
        cmsChange.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
        //更新URL
        cmsChange.setDataUrl(cmsPage.getDataUrl());
        //更新日期
        cmsChange.setPageCreateTime(cmsPage.getPageCreateTime());
        //更新页面类型
        cmsChange.setPageType(cmsPage.getPageType());
        // 执行更新
        cmsPageRepository.save(cmsChange);
        return new CmsPageResult(CommonCode.SUCCESS,cmsChange);
    }

    /**
     * 通过id删除页面
     * @param pageId
     * @return
     */
    public ResponseResult delete(String pageId) {
        if(this.findById(pageId)==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        cmsPageRepository.deleteById(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 页面静态化
     *
     */
    public String getPageHtml(String pageId){
        //获取数据模型。
        Map model = this.getModelByPageId(pageId);
        if(model==null){
            //根据页面的数据url获取不到数据
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //静态化程序获取页面的模板信息
        String templateContent = this.getTemplateByPageId(pageId);
        if(templateContent==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行页面静态化
        return this.generateHtml(model,templateContent);
    }

    //获取数据模型。
    private Map getModelByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if(null==cmsPage){  //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //静态化程序获取页面的DataUrl
        String dataUrl = cmsPage.getDataUrl();
        if(null==dataUrl){
            //从页面信息中找不到获取数据的url
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //静态化程序远程请求DataUrl获取数据模型。
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        return forEntity.getBody();
    }

    //静态化程序获取页面的模板信息
    private String getTemplateByPageId(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage==null){  //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String templateId = cmsPage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){    //模板ID为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEIDISNULL);
        }
        //查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate template = optional.get();

            String templateFileId = template.getTemplateFileId();

            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            if (gridFSFile != null) {
                //打开一个下载流对象
                GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
                //创建GridFsResource对象，获取流
                GridFsResource gridFsResource = new GridFsResource(gridFSFile,downloadStream);
                //从流中取数据
                try {
                    return IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 执行静态化
     * @param model 模型数据
     * @param templateContent  模板
     * @return
     */
    private String generateHtml(Map model,String templateContent){
        //创建一个配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //向configuration中配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板内容
        String content = null;
        try {
            Template template = configuration.getTemplate("template");
            //调用API执行静态化
            content=FreeMarkerTemplateUtils.processTemplateIntoString(template,model);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 页面发布
     * @param pageId 页面id
     * @return
     */
    public ResponseResult postPage(String pageId){
        //执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化的文件存储到GridFs中
        CmsPage cmsPage = this.savePageHtml(pageId, pageHtml);
        //向mq发消息
        this.sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 向mq发消息
     * @param pageId
     */
    private void sendPostPage(String pageId){
        //拼装消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //将pageId转成json
        String jsonString = JSON.toJSONString(msg);
        //获取routingKey(就是页面的站点)
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String routingKey= cmsPage.getSiteId();
        //发送mq
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,routingKey,jsonString);
    }

    /**
     * 保存页面静态化内容
     * @param pageId
     * @param htmlContent
     * @return
     */
    private CmsPage savePageHtml(String pageId,String htmlContent){
        //查询页面
        CmsPage cmsPage = this.findById(pageId);
        if(this.findById(pageId)==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        ObjectId objectId=null;
        try {
            //将htmlContent转为输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html页面内容保存到GridFs
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将html文件id更新到CMSPage中
        assert objectId != null : "objectId为空";
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    //保存页面，有则更新，没有则添加
    public CmsPageResult save(CmsPage cmsPage) {
        //判断页面是否存在
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(one!=null){
            //进行更新
            return this.update(one.getPageId(), cmsPage);
        }
        return this.add(cmsPage);
    }

    //一键发布页面
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面信息存储到cms_page 集合中
        CmsPageResult pageResult = this.save(cmsPage);
        if(!pageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage one = pageResult.getCmsPage();
        String pageId = one.getPageId();
        //执行页面发布，(静态化，保存到GridFs，向mq发消息)
        ResponseResult responseResult = this.postPage(pageId);
        if(!responseResult.isSuccess()){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_SAVEHTMLERROR);
        }
        //拼装页面urlUrl= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        //获取站点信息
        CmsSite cmsSite = this.findCmsSiteById(one.getSiteId());
        if(cmsSite==null){
            ExceptionCast.cast(CmsCode.CMS_SITE_NOTEXISTS);
        }
        String pageUrl = cmsSite.getSiteDomain()+cmsSite.getSiteWebPath()+one.getPageWebPath()+one.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }
    //根据id查询站点
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        return optional.orElse(null);
    }
}
