package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户中心业务层
 */
@Service
public class UserService {
    @Autowired
    private XcUserRepository xcUserRepository;
    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    private XcMenuMapper xcMenuMapper;

    //根据用户名查询用户信息
    public XcUserExt getUserExtByUsername(String username){
        //根据账户查询XcUser信息
        XcUser xcUser = this.getUserByUsername(username);
        if(xcUser==null){
            return null;
        }

        //根据用户id查询用户所属的公司id
        XcCompanyUser xcCompanyUser = this.getCompanyUserByUserId(xcUser.getId());
        String xcCompanyId = null;
        if(StringUtils.isNotEmpty(xcCompanyUser.getCompanyId())){
            //取到用户公司的id
            xcCompanyId = xcCompanyUser.getCompanyId();
        }
        //查询用户的所有权限
        List<XcMenu> permissions = this.selectPermissionsByUserId(xcUser.getId());

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setCompanyId(xcCompanyId);
        xcUserExt.setPermissions(permissions);
        return xcUserExt;
    }

    //根据用户id查询用户所属的公司id
    private XcCompanyUser getCompanyUserByUserId(String userId){
        return xcCompanyUserRepository.findByUserId(userId);
    }
    //根据账户查询XcUser信息
    private XcUser getUserByUsername(String username){
        return xcUserRepository.findByUsername(username);
    }
    //查询用户的所有权限
    private List<XcMenu> selectPermissionsByUserId(String id){
        return xcMenuMapper.selectPermissionsByUserId(id);
    }
}
