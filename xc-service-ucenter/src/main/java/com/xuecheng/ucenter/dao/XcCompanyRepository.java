package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompany;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 公司表持久层
 */
public interface XcCompanyRepository extends JpaRepository<XcCompany, String> {
}
