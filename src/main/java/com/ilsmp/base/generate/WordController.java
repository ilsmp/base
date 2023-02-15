package com.ilsmp.base.generate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import com.ilsmp.base.config.SwaggerConfig;
import com.ilsmp.base.util.Response;
import com.ilsmp.base.util.TimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*
 * Author: zhangjiahao04
 * Description: 接口文档下载 API
 * Date: 2021/11/13 00:57
 * Param:
 * return:
 **/
@RestController
@RequestMapping(value = "/zjh", produces =
        {APPLICATION_OCTET_STREAM_VALUE})
@Api(tags = "All 接口文档 API", description = "接口文档 API")
@ApiResponses(value = {@ApiResponse(code = 200, message = "请求成功,响应成功", response = Response.class)})
@ConditionalOnExpression("${spring.base.api-doc:true}")
@AutoConfigureAfter(SwaggerConfig.class)
public class WordController {

    private String fileName = "接口文档:";

    @Resource
    private WordService wordService;

    /**
     * 将 swagger 文档一键下载为 doc 文档
     *
     * @param model
     * @param swaggerJsonUrl
     *         需要转换成 word 文档的资源地址
     * @param response
     */
    @ApiOperation(value = "将 swagger json url地址转换成 word文档并下载")
    @RequestMapping(value = "/url/doc", method = RequestMethod.GET)
    public void word(Model model, HttpServletRequest request, HttpServletResponse response,
                     @ApiParam(value = "swagger json url资源地址 集群中ip为serverName",
                             defaultValue = "http://localhost:port/前缀/v2/api-docs?group=publicApi", required = true)
                     @RequestParam(value = "swaggerJsonUrl", required = true) String swaggerJsonUrl) throws Exception {
        Map<String, Object> result = wordService.getTableFromUrl(swaggerJsonUrl);
        model.addAttribute("url", swaggerJsonUrl);
        model.addAllAttributes(result);
        String content = wordService.processForContent(model.asMap());
        writeContentToResponse(content, request, response);
    }

    /**
     * 将 swagger json文件转换成 word文档并下载
     *
     * @param model
     * @param swaggerJsonFile
     *         需要转换成 word 文档的swagger json文件
     * @param response
     * @return
     */
    @ApiOperation(value = "将 swagger json文件转换成 word文档并下载")
    @RequestMapping(value = "/file/doc", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            method = RequestMethod.POST)
    public void getWord(Model model, HttpServletRequest request, HttpServletResponse response,
                        @ApiParam(value = "swagger json文件", defaultValue = "swagger json文件", required = true)
                        @Valid @RequestPart(value = "swaggerJsonFile", required = true)
                                MultipartFile swaggerJsonFile) throws Exception {
        Map<String, Object> result = wordService.getTableFromJsonFile(swaggerJsonFile);
        fileName = swaggerJsonFile.getOriginalFilename();
        if (fileName != null) {
            fileName = fileName.replaceAll(".json", "");
        } else {
            fileName = "ProjectInterface:";
        }
        model.addAttribute("url", "http://");
        model.addAllAttributes(result);
        String content = wordService.processForContent(model.asMap());
        writeContentToResponse(content, request, response);
    }

    /**
     * 将 swagger json字符串转换成 word文档并下载
     *
     * @param model
     * @param swaggerJsonStr
     *         需要转换成 word 文档的swagger json字符串
     * @param response
     * @return
     */
    @ApiOperation(value = "将 swagger json字符串转换成 word文档并下载")
    @RequestMapping(value = "/string/doc", method = RequestMethod.POST)
    public void getWord(Model model, HttpServletRequest request, HttpServletResponse response,
                        @ApiParam(value = "swagger json字符串", defaultValue = "swagger json字符串", required = true)
                        @RequestBody(required = true) String swaggerJsonStr)
            throws Exception {
        Map<String, Object> result = wordService.getTableListFromJson(swaggerJsonStr);
        model.addAttribute("url", "http://");
        model.addAllAttributes(result);
        String content = wordService.processForContent(model.asMap());
        writeContentToResponse(content, request, response);
    }

    private void writeContentToResponse(String content, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType(APPLICATION_OCTET_STREAM_VALUE);
        request.setCharacterEncoding(String.valueOf(StandardCharsets.UTF_8));
        response.setCharacterEncoding(String.valueOf(StandardCharsets.UTF_8));
        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        fileNameAccept(request);
        response.setHeader("Content-disposition", "attachment;filename=" +
                fileName + TimeUtil.obtainCurrentTimeNum() + ".doc");
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        bos.write(bytes, 0, bytes.length);
        bos.flush();
    }

    private void fileNameAccept(HttpServletRequest request) throws UnsupportedEncodingException {
        if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
            // firefox浏览器
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        } else if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
            // IE浏览器
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        } else if (request.getHeader("User-Agent").toUpperCase().indexOf("CHROME") > 0) {
            // 谷歌
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        } else {
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        }
    }
}
