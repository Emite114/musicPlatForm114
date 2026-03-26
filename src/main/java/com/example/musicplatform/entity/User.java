package com.example.musicplatform.entity;


import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;



import java.time.LocalDateTime;


@Data  // Lombok注解，自动生成getter、setter、toString等方法
@Entity  // 标识这是一个JPA实体类
@Table(name = "TB_USER")  // 指定对应的数据库表名
@DynamicInsert  // 插入时只生成有值字段的SQL
@DynamicUpdate  // 更新时只生成有变化字段的SQL
public class User {

    /**
     * 主键ID，自增
     */
    @Id  // 标识为主键
    @GeneratedValue(generator = "generatePk")
    // 自增策略（对应MySQL的AUTO_INCREMENT）
    private Long id;


    /**
     * 用户名（唯一）
     */
    @Column(name = "username", unique = true, nullable = false, length = 30)
    // name：对应数据库字段名；unique：唯一约束；nullable：非空；length：长度限制
    private String username;

    /**
     * 加密后的密码
     */
    @Column(name = "password", nullable = false, length = 60)
    private String password;

    /**
     * 邮箱（唯一）
     */
    @Column(name = "email", unique = true, length = 50)
    private String email;

    /*
     * 手机号（唯一）
     */
//    @Column(name = "phone_number", unique = true, length = 11)
//    private String phoneNumber;  // 注意：数据库字段是phone_number，实体类用驼峰命名

    /*
     * 注册时间
     */
    @Column(name = "register_time", updatable = false)
    // updatable = false：该字段不参与更新操作（注册时间一旦设置就不会改变）
    private LocalDateTime registerTime;

//头像
    @Column(name = "avatar_url", length = 200)
    private String avatarUrl;

    /**
     * 性别（male/female/unknown）
     */
    @Column(name = "gender")
    @Enumerated(EnumType.STRING)  // 存储枚举的字符串值（而非默认的索引）
    private GenderEnum gender;

    /**
     * 是否激活（0-未激活，1-已激活）
     */
    @Column(name = "is_active")
    private Boolean isActive = true;  // 默认未激活（0）

    /**
     * 性别枚举（限制取值范围）
     */
    public enum GenderEnum {
        male, female, unknown
    }

    /**
     * 默认值
     */
    @PrePersist  // JPA生命周期注解：在插入数据库之前执行
    public void prePersist() {
        if (registerTime == null) {
            registerTime = LocalDateTime.now();  // 自动设置当前时间
        }
        if (gender==null||this.gender==null){
            this.gender= GenderEnum.unknown;
        }
        if(isActive==null||this.isActive==null){
            this.isActive=true;
        }

    }





}
