package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value="文件系统管理接口",description = "文件的上传、下载")
public interface FileSystemControllerApi {

    @ApiOperation("上传文件接口")
    /**
     * 参数信息：
     *  multipartFile 上传的文件本身
     *  fileTag 文件的标签
     *  businessKey 文件的标识
     *  metaData    文件的元信息，上传过来的事json数据，后面会转map
     */
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String fileTag,
                                   String businessKey,
                                   String metaData);
}
