package com.cosmos.diseaseforecasterpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.pojo.User;
import com.cosmos.diseaseforecasterpro.mapper.UserMapper;
import com.cosmos.diseaseforecasterpro.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-02-27
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper mapper;


    //盐值 用于混淆加密
    private static final String SALT = "cosmos";


    /**
     * 用户登录态的键
     */
    public static final String USER_LOGIN_STATE = "userLoginState";
    private static final int ADMIN_CODE = 1;

    @Override
    public Result userRegister(String account, String password, String checkPassword) {
        //1.校验是否为空
        if (StringUtils.isAnyBlank(account, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能为空");
        }

        if (account.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能小于4位");
        }

        if (password.length() < 6 || checkPassword.length() < 6) {
            return Result.error("密码不能小于6位");

        }


        //2.检验是否包含特殊字符
        //匹配字符差串中是否包含特殊字符
        String validPattern = "[^a-zA-Z0-9]";
        Matcher matcher = Pattern.compile(validPattern).matcher(account);
        if (matcher.find()) {
            return Result.error("不能包含特殊字符");
        }

        //3.检验密码是否相同
        if (!password.equals(checkPassword)) {
            return Result.error("密码不相同");
        }


        //4.检验是否存在相同账号的成员，这里可以直接简要的写一个查询不必封装
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getUserAccount, account);

        long count = 0;
        //直接查找数目，不用返回数据了
        count = mapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("已有账户存在");
        }

        //5.加密密码
        //用springboot自带的方法进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        //6.插入数据
        User newUser = new User();
        newUser.setUserAccount(account);
        newUser.setUserPassword(encryptPassword);
        int result = mapper.insert(newUser);
        if (result <= 0)//代表操作失败
        {
            return Result.error("插入数据错误，注册失败");
        }
//MyBatis-Plus在执行插入操作后，会从数据库中获取自动生成的ID，并将其设置回插入的对象
        return Result.success("恭喜你注册成功!!");
    }

    @Override
    public Result userLogin(String account, String password, HttpServletRequest request) {

        //1.校验是否为空
        if (StringUtils.isAnyBlank(account, password)) {
            log.info(account, password);
            return Result.error("不能为空");
        }

        //2.检验账号密码长度
        if (account.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能小于4位");
        }

        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能小于6位");

        }
        //3.匹配字符差串中是否包含特殊字符
        String validPattern = "[^a-zA-Z0-9]";
        Matcher matcher = Pattern.compile(validPattern).matcher(account);
        if (matcher.find()) {
            return Result.error("不能包含特殊字符");
        }

        //4.加密密码
        //用springboot自带的方法进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        //5.查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getUserAccount, account)
                .eq(User::getUserPassword, encryptPassword);

        User user;
        user = mapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        } else {

            //脱敏
            //这里创建一个新的用户对象用于返回部分值到前端
            User safetyUser = getSafetyUser(user);

            HttpSession session = request.getSession();
            log.info("Session ID: {}", session.getId());
            session.setAttribute(USER_LOGIN_STATE, safetyUser);


            //往session中设置的登录状态
//            request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
            log.info("存储Session成功,{}", safetyUser.toString());

            return Result.success("登录成功！！", safetyUser);
        }

    }

    @Override
    public User getSafetyUser(User origenUser) {
        User safetyUser = new User();
        safetyUser.setId(origenUser.getId());
        safetyUser.setUsername(origenUser.getUsername());
        safetyUser.setAvatarUrl(origenUser.getAvatarUrl());
        safetyUser.setGender(origenUser.getGender());
        safetyUser.setPhone(origenUser.getPhone());
        safetyUser.setRole(origenUser.getRole());
        safetyUser.setEmail(origenUser.getEmail());
        return safetyUser;

    }

    @Override
    public Result userLayout(HttpServletRequest request) {

        if (request.getSession().getAttribute(USER_LOGIN_STATE) != null) {
            request.getSession().removeAttribute(USER_LOGIN_STATE);
            return Result.success("账号已退出");
        }
        //移除登录态
        throw new BusinessException(ErrorCode.NO_AUTH, "未登录");
    }

    @Override
    public long getUserId(HttpServletRequest request) {

        //从session中获取用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH, "未登录");
        }

        //强转
        User user = (User) userObj;


        return user.getId();
    }

    @Override
    public Boolean updateUser(User user, HttpServletRequest request) {

        Long userId = getUserId(request);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, userId);

        if (!StringUtils.isBlank(user.getUserPassword())) {
            if (user.getUserPassword().length() < 6) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能小于6位");
            }
            //加密密码
            user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes()));
        }


        int result = mapper.update(user, wrapper);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新失败");
        }


        //重新存放缓存
        User newUser = this.getById(userId);
        if (newUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新失败");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, this.getSafetyUser(newUser));

        return true;
    }

    @Override
    public List<User> selectAllByUsername(String username, HttpServletRequest request) {


        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "非管理员，权限不足");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.like(User::getUsername, username);


        List<User> userList = mapper.selectList(wrapper);
        log.error(username);
        log.error(userList.toString());
        //脱敏
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public Result deleteUserById(Integer id, HttpServletRequest request) {

        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入要删除用户的id");
        }

        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "非管理员，权限不足");
        }

        int affectRows = mapper.deleteById(id);
        if (affectRows > 0)
            return Result.success("删除成功");
        return Result.error("删除失败");
    }


    /**
     * 判断是否为管理员，将公共部分抽出来成为一个函数
     *
     * @param request 请求参数
     * @return 是否为管理员 是true 否false
     */
    private boolean isAdmin(HttpServletRequest request) {
        //从session中获取用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        //强转

        log.error(String.valueOf((User) userObj));


        User user = (User) userObj;

        return user != null && user.getRole() == ADMIN_CODE;
    }


}
