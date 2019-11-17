package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 课程管理的自定义异常类型，其中定义异常类型所对应的错误代码
 */
@RestControllerAdvice //控制器增强
public class CustomerException extends ExceptionCatch {
    static {
        //权限不足
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
