package com.ilsmp.base;


import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author: ZJH Title: BaseEntity Package com.zhihui.gongdi.tool Description: 基础实体类 Date 2020/3/19 18:12
 * EntityListeners（）会调用com.zhihui.gongdi.config.UserAuditor 不加set/get方法返回值中没有基类中字段
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Setter
@Getter
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键 nullable : false default : mysql/sqlserver:strategy = GenerationType.IDENTITY;oracle用序列
     * columnDefinition：postgresql不支持comment注释
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Schema(name = "主键id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true,
            type = "Long", example = "12345678", defaultValue = "12345678")
    @Column(name = "id", nullable = false, columnDefinition = "bigint COMMENT '主键'")
    protected Long id;

}
