package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * cmsPage业务层
 */
@Service
public class CmsPageService {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(CmsPageService.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

//    @Autowired
//    private RabbitTemplate rabbitTemplate;

    //保存HTML页面到服务器物理路径
    public void savePageToServerPath(String pageId){
        //根据pageId获取CMSPage
        CmsPage cmsPage = this.findCmsPageById(pageId);
        //从CMSPage中获取HTMLField
        String htmlFileId = cmsPage.getHtmlFileId();
        //从GridFs中查询HTML文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if(inputStream==null){
            LOGGER.error("getFileById InputStream is null ,htmlFileId:{}",htmlFileId);
            return;
        }
        //得到站点id
        String siteId = cmsPage.getSiteId();
        //得站点的信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //得到站点的物理路径
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        //得到页面的物理路径
        String pagePath = sitePhysicalPath+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();
        //将html文件保存到服务器物理路径上
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(pagePath);
            IOUtils.copy(inputStream,outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                assert null != outputStream : "outputStream为空";
                outputStream.close();
                inputStream.close();
//                rabbitTemplate.convertAndSend(cmsPage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //根据文件id从GridFs中查询文件内容
    private InputStream getFileById(String htmlFileId){
        //文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        //打开下载流
        if(gridFSFile==null){
            LOGGER.error("gridFsFile is null ,htmlFileId:{}",htmlFileId);
            return null;
        }
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义GridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //根据页面id查询页面
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        return optional.orElse(null);
    }

    //根据站点id查询站点
    private CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        return optional.orElse(null);
    }
}
