package com.example.musicplatform.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data  // Lombok注解，自动生成getter、setter、toString等方法
@Entity  // 标识这是一个JPA实体类
@Table(name = "TB_USER_ROLE")  // 指定对应的数据库表名
@DynamicInsert  // 插入时只生成有值字段的SQL
@DynamicUpdate  //
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30,unique = false,nullable = false)
    private Long userId;

    @Column(length = 30,unique = false,nullable = false)
    private Long roleId;



}
