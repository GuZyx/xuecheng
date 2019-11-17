package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 文件系统业务层
 */
@Service
public class FileSystemService {
    @Autowired
    private FileSystemRepository fileSystemRepository;

    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    private int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    private int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    private String charset;
    @Value("${xuecheng.fastdfs.tracker_servers}")
    private String tracker_servers;

    //上传文件
    public UploadFileResult upload(MultipartFile multipartFile, String fileTag,
                                   String businessKey, String metaData) {
        if(multipartFile==null){
            //上传文件为空
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        //第一步，将文件上传到fastDFS中得到一个文件id
        String fileId= this.fdfs_upload(multipartFile);
        if(fileId==null){
            //上传文件失败
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        //第二部，将文件id及其他信息存储到MongoDB中
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setFiletag(fileTag);
        fileSystem.setBusinesskey(businessKey);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFileType(multipartFile.getContentType());
        if(StringUtils.isNotEmpty(metaData)){
            try {
                Map map = JSON.parseObject(metaData, Map.class);
                fileSystem.setMetadata(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    /**
     * 上传文件到fastDFS
     * @param multipartFile 文件本身
     * @return 文件id
     */
    private String fdfs_upload(MultipartFile multipartFile){
        //初始化fastDFS环境
        this.initFdfsConfig();
        //创建trackerClient，为了连接上storage服务
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            //创建trackerServer
            trackerServer = trackerClient.getConnection();
            //连接上storageServer
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            //创建一个storageClient，用来上传文件
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storageServer);
            //获取字节
            byte[] bytes = multipartFile.getBytes();
            //得到文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            //根据原始名称得到拓展名
            String file_ext_name = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            /**
             *  上传文件并获取文件id
             * 参数：
             *  bytes   字节
             *  file_ext_name   拓展名
             *  meta_list   元数据
             */
            String fileId = storageClient1.upload_file1(bytes, file_ext_name, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                assert trackerServer != null;
                trackerServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //初始化fastDFS的环境
    private void initFdfsConfig(){
        try {
            //初始化tracker服务器(多个tracker中间以英文逗号分隔)
            ClientGlobal.initByTrackers(tracker_servers);
            //初始化字符集
            ClientGlobal.setG_charset(charset);
            //设置连接时间
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            //设置网络时间
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
            //抛出异常
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
    }

}
