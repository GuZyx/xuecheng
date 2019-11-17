package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.controller.MediaUploadController;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * 媒资上传业务层
 */
@Service
public class MediaUploadService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadController.class);

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //得到文件所属目录路径
    private String getFileFolderPath(String fileMd5) {
        return  upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }

    //得到文件的路径
    private String getFilePath(String fileMd5,String fileExt){
        return this.getFileFolderPath(fileMd5)+fileMd5+"."+fileExt;
    }

    //得到块文件的所属路径
    private String getChunkFileFolderPath(String fileMd5){
        return this.getFileFolderPath(fileMd5)+"chunks/";
    }

    /** 文件上传前的注册
     * 根据文件md5得到文件路径
     * * 规则：
     * *
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符 *
     * 三级目录：md5 *
     * 文件名：md5+文件扩展名 *
     * @param fileMd5 文件md5值 *
     * @param fileExt 文件扩展名 *
     * @return 文件路径
     * */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize,
                                   String mimetype, String fileExt) {
        //1.校验文件在磁盘上是否存在
        //文件所属目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件的路径
        String filePath = this.getFilePath(fileMd5,fileExt);
        File file = new File(filePath);
        //文件是否存在
        boolean exists = file.exists();

        //2.检查文件信息在MongoDB是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);

        if(exists && optional.isPresent()){
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        //文件不存在，做一些准备工作，检查文件所在的目录是否存在
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            boolean mkdirs = fileFolder.mkdirs();
            if(!mkdirs){    //上传文件目录创建失败
                ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_CREATEFOLDER_FAIL);
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 检查分块是否存在
     * @param fileMd5   块文件的md5
     * @param chunk 块文件的下标
     * @param chunkSize 块的大小
     * @return  CheckChunkResult
     */
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //得到分块文件的所在目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //块文件
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if(chunkFile.exists()){
            //块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else{
            //不存在
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }

    //上传分块
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        //检查分块目录，如果不存在要自动创建
        //得到分块目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到分块文件路径
        String chunkFilePath = chunkFileFolderPath+chunk ;

        File chunkFileFolder = new File(chunkFileFolderPath);
        //如果不存在要自动创建
        if(!chunkFileFolder.exists()){
            boolean mkdirs = chunkFileFolder.mkdirs();
            if(!mkdirs){    //创建失败
                ExceptionCast.cast(MediaCode.UPLOAD_CHUNK_REGISTER_CREATEFOLDER_FAIL);
            }
        }
        //得到文件的输入流
        InputStream inputStream =null;
        FileOutputStream fileOutputStream=null;
        try {
            inputStream = file.getInputStream();
            fileOutputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                assert fileOutputStream!=null:"输出流为空";
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //合并文件
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize,
                                      String mimetype, String fileExt) {
        //1.合并所有的分块
        //得到分块文件的目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        //分块文件文件列表
        File[] files = chunkFileFolder.listFiles();
        if(files==null){
            LOGGER.error("分块列表为空");
            ExceptionCast.cast(MediaCode.CHUNK_FILE_ISNULL);
        }
        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        //执行合并
        mergeFile=this.mergeFile(Arrays.asList(files),mergeFile);
        if(mergeFile==null){
            //合并文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //2.校验文件的md5是否和前端传入的md5一致
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if(!checkFileMd5){
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //3.将文件的信息写入MongoDB
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        //文件相对路径
        mediaFile.setFilePath(fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/");
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        //向mq发送视频处理消息
        ResponseResult responseResult = this.sendProcessVideoMsg(mediaFile.getFileId());
        return responseResult;
    }

    //发送视频处理消息
    public ResponseResult sendProcessVideoMsg(String mediaId){
        //查询数据库
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //构建消息内容
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String jsonString = JSON.toJSONString(map);

        //向mq发送视频处理消息
        try{
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //执行合并
    private File mergeFile(List<File> chunkFileList,File mergeFile){
        try {
            //如果合并的文件已存在，删除，否则创建新文件
            if(mergeFile.exists()){
                mergeFile.delete();
            }else {
                //创建一个新文件
                mergeFile.createNewFile();
            }

            //对文件进行排序
            chunkFileList.sort(new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });
            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            //缓冲区
            byte[] bytes = new byte[1024];
            for (File chunkFile:chunkFileList){
                //创建一个读对象
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = raf_read.read(bytes))!=-1){
                    raf_write.write(bytes,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //校验文件的md5值
    private boolean checkFileMd5(File mergeFile,String md5){
        //创建文件的输入流
        try {
            FileInputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的md5
            String md5Hex = DigestUtils.md5Hex(inputStream);
            //和传入的md5进行比较
            if(md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
