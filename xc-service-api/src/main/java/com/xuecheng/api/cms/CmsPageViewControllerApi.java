package com.xuecheng.api.cms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="页面预览接口",description = "cms页面预览")
public interface CmsPageViewControllerApi {
    @ApiOperation("页面预览")
    public void preview(String pageId);
}
