package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service    //课程分类业务层
public class CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    //课程分类查询
    public CategoryNode findCategoryList() {
        return categoryMapper.findCategoryList();
    }
}
