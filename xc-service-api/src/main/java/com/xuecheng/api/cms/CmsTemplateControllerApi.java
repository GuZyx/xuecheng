package com.xuecheng.api.cms;

import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="cms模板管理接口",description = "cms模板接口的增、删、改、查")
public interface CmsTemplateControllerApi {
    //页面查询
    @ApiOperation("模板查询列表")
    public QueryResponseResult findList();

}
