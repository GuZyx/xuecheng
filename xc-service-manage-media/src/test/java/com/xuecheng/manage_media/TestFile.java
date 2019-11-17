package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {
    @Test    //测试文件的分块
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\test_ffmpeg\\lucene.avi");
        //块文件的目录
        String chunkFileFolder="D:\\test_ffmpeg\\chunks\\";

        //定义块文件的大小
        long chunkFileSize = 1024 * 1024;

        //块数    ceil向上转型
        int chunkFileNumber =(int) Math.ceil(sourceFile.length()*1.0/chunkFileSize);

        //创建读文件的对象  随机读
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile,"r");

        //缓冲区
        byte[] b = new byte[1024];
        for(int i=0;i<chunkFileNumber;i++){
            //块文件
            File chunkFile = new File(chunkFileFolder+i);

            RandomAccessFile raf_write = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            while ((len = raf_read.read(b))!=-1){
                //向块文件写对象
                raf_write.write(b,0,len);
                //如果块文件的大小达到了1mb，可以开始写下一块
                if(chunkFile.length()>=chunkFileSize){
                    break;
                }
            }
            raf_write.close();
        }
        raf_read.close();
    }

    @Test   //测试文件的合并
    public void testMergeFile() throws IOException {
        //块文件的目录
        String chunkFileName = "D:\\test_ffmpeg\\chunks\\";
        //块文件的目录对象
        File chunkFileFolder = new File(chunkFileName);
        //块文件的列表
        File[] files = chunkFileFolder.listFiles();
        //将块文件排序,按名称升序
        List<File> fileList = Arrays.asList(files);
        fileList.sort(new Comparator<File>() {
            @Override   //返回-1是升序，返回1是降序
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });

        //合并的文件
        File mergeFile = new File("D:\\test_ffmpeg\\lucene_merge.avi");
        //创建一个新文件
        boolean newFile = mergeFile.createNewFile();

        //写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");

        byte[] b = new byte[1024];
        for(File chunkFile:fileList){
            //创建一个读块文件的对象
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
            int len = -1;
            while ((len=raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}
