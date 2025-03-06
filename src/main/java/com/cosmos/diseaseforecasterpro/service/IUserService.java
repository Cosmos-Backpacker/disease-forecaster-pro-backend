package com.cosmos.diseaseforecasterpro.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.pojo.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-02-27
 */
public interface IUserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param account       用户账号
     * @param password      用户密码
     * @param checkPassword 校验密码
     * @return
     */

    Result userRegister(String account, String password, String checkPassword);

    /**
     * 用户登录
     *
     * @param account  登录账号
     * @param password 登录密码
     * @param request  请求参数，存储session
     * @return
     */
    Result userLogin(String account, String password, HttpServletRequest request);


    /**
     * 用户查询
     *
     * @param username 用户名
     */
    List<User> selectAllByUsername(String username, HttpServletRequest request);

    /**
     * 删除用户
     */
    Result deleteUserById(Integer id, HttpServletRequest request);


    /**
     * 抽出一个方法，给用户信息脱敏
     *
     * @param origenUser 原始用户数据
     * @return 脱敏后的用户数据
     */

    public User getSafetyUser(User origenUser);


    /**
     * 用户注销
     *
     * @param request 请求参数
     * @return 注销结果
     */
    public Result userLayout(HttpServletRequest request);




    public long getUserId(HttpServletRequest request);


}
