package com.ilsmp.base;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "主键id", required = false, allowEmptyValue = true, dataType = "Long", example = "1")
    @Column(name = "id", nullable = false, columnDefinition = "bigint COMMENT '主键'")
    protected Long id;

}
