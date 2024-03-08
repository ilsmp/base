package com.ilsmp.base.auto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Author: zhangjiahao04 Title: BaseProperties Package: com.ilsmp.base.tool.auto Description: 基础配置 Date: 2020/5/9 13:33
 */
@Data
@ConfigurationProperties(prefix = "spring.base", ignoreInvalidFields = true)
public class BaseProperties {
    /*
     * Author: zhangjiahao04
     * Description: 用户ID标识的字段名
     * Date: 2020/5/9 14:45
     * Param:
     * return:
     **/
    private String userId;

    /*
     * Author: zhangjiahao04
     * Description: 实体类扫描包
     * Date: 2020/5/9 14:45
     * Param:
     * return:
     **/
    private String entityScan;

    /*
     * Author: zhangjiahao04
     * Description: 配置类扫描包
     * Date: 2020/5/9 14:45
     * Param:
     * return:
     **/
    private String componentScan;

    /*
     * Author: zhangjiahao04
     * Description: nacos server ip 配置
     * Date: 2020/5/9 14:45
     * Param:
     * return:
     **/
    private String nacosAddr;

    /*
     * Author: zhangjiahao04
     * Description: 最大超时时间s
     * Date: 2020/5/9 19:52
     * Param:
     * return:
     **/
    private Long allowAge;

    /*
     * Author: zhangjiahao04
     * Description: 是否允许携带cookie
     * Date: 2020/5/9 19:57
     * Param:
     * return:
     **/
    private Boolean allowCredential;

    /*
     * Author: zhangjiahao04
     * Description: 允许哪些类型请求
     * Date: 2020/5/9 19:56
     * Param:
     * return:
     **/
    private String allowMethod;

    /*
     * Author: zhangjiahao04
     * Description: 允许的请求头
     * Date: 2020/5/9 19:56
     * Param:
     * return:
     **/
    private String allowHeader;

    /*
     * Author: zhangjiahao04
     * Description: 允许跨域白名单
     * Date: 2020/5/9 19:38
     * Param:
     * return:
     **/
    private String allowOrigin;

    /*
     * Author: zhangjiahao04
     * Description: 是否允许打开在线接口文档
     * Date: 2020/5/9 19:57
     * Param:
     * return:
     **/
//    private Boolean apiEnable;
//    private Boolean swagger3Adapter;

    /*
     * Author: zhangjiahao04
     * Description: 是否允许生成线下接口文档
     * Date: 2020/5/9 19:57
     * Param:
     * return:
     **/
//    private Boolean apiDoc;

    /*
     * Author: zhangjiahao04
     * Description: 是否允许多数据库配置
     * Date: 2020/5/9 19:57
     * Param:
     * return:
     **/
    private Boolean multiDb;
}
