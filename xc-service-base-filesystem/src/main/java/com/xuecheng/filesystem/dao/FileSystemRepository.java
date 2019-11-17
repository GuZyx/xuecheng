package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * 文件系统持久层
 */
@Repository
public interface FileSystemRepository extends MongoRepository<FileSystem,String> {
}
