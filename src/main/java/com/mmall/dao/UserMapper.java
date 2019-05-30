package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    String selectQuestionByUsername(String username);

    int chechAnswer(@Param("username") String username ,@Param("question") String question ,@Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username ,@Param("passwordNew") String passwordNew);

    User selectLogin(@Param("username") String username, @Param("password") String password);

    int checkPassword(@Param("passwordOld") String passwordOld ,@Param("userId") int userId );

    int checkEmailByUserId(@Param("email") String email ,@Param("userId") int userId);
}