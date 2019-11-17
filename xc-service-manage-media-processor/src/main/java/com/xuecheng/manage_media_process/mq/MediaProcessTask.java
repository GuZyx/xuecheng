package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 监听视频处理队列，并进行视频处理
 */
@Component
public class MediaProcessTask {
    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.video-location}")
    private String serverPath;

    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpeg_path;

    //接收视频处理的消息
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg) throws Exception{
        //1.解析消息内容，获取mediaId
        Map msgMap = JSON.parseObject(msg,Map.class);
        String mediaId = (String) msgMap.get("mediaId");
        //2.拿mediaId从数据库查询文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){  //为空
            System.out.println("================恐龙===============");
            return;
        }
        MediaFile mediaFile = optional.get();
        //判断如果类型为空或者不是avi文件不进行处理
        if(!"avi".equals(mediaFile.getFileType())){
            mediaFile.setProcessStatus("303004");//无需处理
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            mediaFile.setProcessStatus("303001");//处理中
            mediaFileRepository.save(mediaFile);
        }
        //3.将AVI转成mp4文件
        //String ffmpeg_path, String video_path, String mp4_name, String mp4folder_path
        String video_path = serverPath+mediaFile.getFilePath()+mediaFile.getFileName();
        String mp4_name = mediaFile.getFileId()+".mp4";
        String mp4folder_path = serverPath+mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        String result = mp4VideoUtil.generateMp4();
        if(!"success".equals(result)){
            //处理失败
            mediaFile.setProcessStatus("303003");
            //定义失败的原因
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

            mediaFileRepository.save(mediaFile);
            return;
        }

        //4.将mp4生成m3u8和ts文件
        //String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path
        //mp4文件的路径
        String mp4_video_path = serverPath+mediaFile.getFilePath()+mp4_name;
        //m3u8文件名称
        String m3u8_name = mediaFile.getFileId()+".m3u8";
        //m3u8文件所在的目录
        String m3u8folder_path = serverPath+mediaFile.getFilePath()+"hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,mp4_video_path,m3u8_name,m3u8folder_path);
        //生成m3u8文件和ts文件
        String tsResult = hlsVideoUtil.generateM3u8();
        if(!"success".equals(tsResult)){
            //处理失败
            mediaFile.setProcessStatus("303003");
            // 定义失败的原因
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(tsResult);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

            mediaFileRepository.save(mediaFile);
            return;
        }
        //处理成功
        //获取ts文件列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        //定义mediaFileProcess_m3u8
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

        mediaFile.setProcessStatus("303002");

        //保存文件url(此url就是视频播放的相对路径)
        String fileUrl = mediaFile.getFilePath()+"hls/"+m3u8_name;
        mediaFile.setFileUrl(fileUrl);

        mediaFileRepository.save(mediaFile);

    }

}
