package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * cms_page 集合前端控制层
 */
@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    private CmsPageService cmsPageService;

    @Override   //分页查询页面
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page,@PathVariable("size") int size, QueryPageRequest queryPageRequest) {
        return cmsPageService.findList(page, size, queryPageRequest);
    }

    @Override   //添加页面
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) { //添加页面
        return cmsPageService.add(cmsPage);
    }

    @Override   //根据id查询页面
    @GetMapping("/get/{id}")
    public CmsPage findById(@PathVariable("id") String pageId) {
        return cmsPageService.findById(pageId);
    }

    @Override   //根据id更改页面
    @PutMapping("/edit/{id}")   //这里用put方法，put在http中表示更新
    public CmsPageResult edit(@PathVariable("id") String pageId,@RequestBody CmsPage cmsPage) {
        return cmsPageService.update(pageId,cmsPage);
    }

    @Override   //根据id删除页面
    @DeleteMapping("/delete/{id}")
    public ResponseResult delete(@PathVariable("id") String pageId) {
        return cmsPageService.delete(pageId);
    }

    @Override   //发布页面
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {
        return cmsPageService.postPage(pageId);
    }

    @Override   //保存页面(有了更新，没有添加)
    @PostMapping("/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return cmsPageService.save(cmsPage);
    }

    @Override   //一键发布页面
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return cmsPageService.postPageQuick(cmsPage);
    }

}
