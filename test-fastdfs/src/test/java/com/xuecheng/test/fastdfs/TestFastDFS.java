package com.xuecheng.test.fastdfs;

import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {
    //文件上传
    @Test
    public void testUpload(){
        try {
            //加载fastdfs-client.properties
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient,用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接TrackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取StorageServer
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            //获取storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storageServer);
            //获取文件地址
            String filePath="d:/logo.jpg";
            //向storage上传
            /**
             * 参数：
             *  文件地址
             *  文件拓展名
             *  元信息
             */
            //上传成功后拿到文件的id
            String fileId = storageClient1.upload_file1(filePath, "jpg", null);
            System.out.println(fileId);
            //group1/M00/00/00/wKjYgV2oVFeAdaW5AABovbqZ-YQ422.jpg
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //文件下载
    @Test
    public void testDownload(){
        try {
            //加载fastdfs-client.properties
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient,用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接TrackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取StorageServer
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            //获取storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storageServer);
            //下载文件
            //文件id
            String fileId = "group1/M00/00/00/wKjYgV2oVFeAdaW5AABovbqZ-YQ422.jpg";
//            group1/M00/00/00/wKjYgV2pgKaAJWqyAAHP87C5phI361.jpg
            byte[] bytes = storageClient1.download_file1(fileId);
            //使用输出流保存文件
            FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/logo.jpg"));
            fileOutputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
