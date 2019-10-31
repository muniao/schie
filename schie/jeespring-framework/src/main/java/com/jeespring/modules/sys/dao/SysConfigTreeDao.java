/**
 * * Copyright &copy; 2015-2020 <a href="https://gitee.com/JeeHuangBingGui/JeeSpring">JeeSpring</a> All rights reserved..
 */
package com.jeespring.modules.sys.dao;

import org.apache.ibatis.annotations.Mapper;

import com.jeespring.common.persistence.TreeDao;
import com.jeespring.modules.sys.entity.SysConfigTree;

/**
 * 系统配置DAO接口
 * 
 * @author JeeSpring
 * @version 2018-08-22
 */
@Mapper
public interface SysConfigTreeDao extends TreeDao<SysConfigTree> {

}