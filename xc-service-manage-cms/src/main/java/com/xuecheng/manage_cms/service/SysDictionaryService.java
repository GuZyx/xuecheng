package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//数据字典业务层
@Service
public class SysDictionaryService {
    @Autowired
    SysDictionaryRepository sysDictionaryRepository;
    public SysDictionary getByType(String type) {
        if(type==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        return sysDictionaryRepository.findByDType(type);
    }
}
