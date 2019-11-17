package com.xuecheng.api.cms;

import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="cms站点管理接口",description = "cms站点接口的查")
public interface CmsSiteControllerApi {
    //页面查询
    @ApiOperation("站点查询列表")
    public QueryResponseResult findList();

}
