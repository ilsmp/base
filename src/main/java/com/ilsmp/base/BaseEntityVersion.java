package com.ilsmp.base;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author: ZJH Title: BaseEntityVersion Package com.zhihui.gongdi.tool Description: Date 2020/3/30 14:39
 * EntityListeners（）会调用com.zhihui.gongdi.config.UserAuditor 不加set/get方法返回值中没有基类中字段
 */

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Setter
@Getter
public class BaseEntityVersion extends BaseEntity {

    /**
     * 上传版本号 nullable : false default  : null
     */
    @AutoValue
    @ApiModelProperty(value = "上传版本号,更新数据时必传", required = false, allowEmptyValue = true, dataType = "Long",
            example = "1586666666000")
    @Column(name = "update_version", nullable = false, columnDefinition = "bigint default 0 COMMENT '上传版本号'")
    protected Long updateVersion;

    /**
     * 是否被删除 nullable : true default  : 0
     */
    @ApiModelProperty(value = "是否被删除,不传", required = false, allowEmptyValue = true, dataType = "Short", example =
            "0")
    @Column(name = "deleted", nullable = true, columnDefinition = "smallint default 0 COMMENT '是否被删除'")
    @JsonIgnore
    protected Short deleted = 0;

    /**
     * 创建时间 nullable : false default  : null
     */
    @CreatedDate
    @ApiModelProperty(value = "创建时间，不传", required = false, allowEmptyValue = true, dataType = "java.sql.timestamp",
            example = "1586666666000")
    @Column(name = "create_timestamp", nullable = false, columnDefinition = "timestamp COMMENT '创建时间'")
    protected Timestamp createTimestamp;

    /**
     * 创建用户 nullable : false default  : null
     */
    @CreatedBy
    @ApiModelProperty(value = "创建用户，不传", required = false, allowEmptyValue = true, dataType = "String", example =
            "admin")
    @Column(name = "create_user", nullable = false, columnDefinition = "varchar(25) COMMENT '创建用户'")
    @JsonIgnore
    protected String createUser;

    /**
     * 最后修改时间 nullable : false default  : null
     */
    @LastModifiedDate
    @ApiModelProperty(value = "最后修改时间，不传", required = false, allowEmptyValue = true,
            dataType = "java.sql.timestamp", example = "1586666666000")
    @Column(name = "modify_timestamp", nullable = false, columnDefinition = "timestamp COMMENT '最后修改时间'")
    protected Timestamp modifyTimestamp;

    /**
     * 最后修改用户 nullable : false default  : null
     */
    @LastModifiedBy
    @ApiModelProperty(value = "最后修改用户，不传", required = false, allowEmptyValue = true, dataType = "String",
            example = "admin")
    @Column(name = "modify_user", nullable = false, columnDefinition = "varchar(25) COMMENT '最后修改用户'")
    @JsonIgnore
    protected String modifyUser;

}
