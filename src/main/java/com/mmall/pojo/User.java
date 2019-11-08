package com.mmall.pojo;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements Serializable {
    private Integer id ;

    private String username;

    private String password;

    private String email = "";

    private String phone = new String();

    private String question;

    private String answer;

    private Integer role;

    private Date createTime;

    private Date updateTime;

}