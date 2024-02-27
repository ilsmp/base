package com.ilsmp.base.generate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ilsmp.base.generate.entity.Model;
import com.ilsmp.base.generate.entity.Request;
import com.ilsmp.base.generate.entity.Table;
import com.ilsmp.base.util.JsonUtil;
import com.ilsmp.base.util.Response;
import com.ilsmp.base.util.RestTemplateUtil;
import com.ilsmp.base.util.StringUtil;
import com.ilsmp.base.util.TimeUtil;
import com.jayway.jsonpath.DocumentContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/*
 * Author: zhangjiahao04
 * Description: 接口文档下载 service
 * Date: 2021/11/13 00:57
 * Param:
 * return:
 **/
@Slf4j
@Service
public class WordService {

    public static final String SCHEMA = "schema";
    public static final String ITEMS = "items";
    public static final String CONSUMES = "consumes";
    public static final String PRODUCES = "produces";
    public static final String FORMAT = "format";
    public static final String DESCRIPTION = "description";
    public static final String ARRAY = "array";
    public static final String OBJECT = "object";
    public static final String PARAMETERS = "parameters";
    private String definePath = "#/components/schemas/";
    @Resource
    private SpringTemplateEngine springTemplateEngine;
    /*
     * Author: zhangjiahao04
     * Description: swagger版本
     * Date: 2022/5/19 14:34
     **/
    private String swaggerVersion;
    /*
     * Author: zhangjiahao04
     * Description: 数据存储
     * Date: 2022/5/19 17:13
     * Param:
     * return:
     **/
    private DocumentContext documentContext;

    /**
     * 将map转换成url
     */
    public static String getUrlParamsByMap(Map<String, Object> map) throws Exception {
        if (StringUtil.isEmpty(map)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append("&");
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = StringUtil.substringBeforeLast(s, "&");
        }
        return s;
    }

    /**
     * 将map转换成header
     */
    public static String getHeaderByMap(Map<String, Object> map) {
        if (StringUtil.isEmpty(map)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("-H \"");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
            sb.append("\" ");
        }
        return sb.toString();
    }

    public Map<String, Object> getTableFromUrl(String swaggerUrl) throws Exception {
        String jsonStr = RestTemplateUtil.getRestTemplate().getForObject(swaggerUrl, String.class);
        return getTableListFromJson(jsonStr);
    }

    public Map<String, Object> getTableListFromJson(String jsonStr) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> map = JsonUtil.readValue(jsonStr, HashMap.class);
        List<Table> result = getResult(map);
        Map<String, List<Table>> tableMap = result.stream().parallel().
                collect(Collectors.groupingBy(Table::getTitle));
        resultMap.put("tableMap", new TreeMap<>(tableMap));
        resultMap.put("info", map.get("info"));
        return resultMap;
    }

    public Map<String, Object> getTableFromJsonFile(MultipartFile jsonFile) throws Exception {
        String jsonStr = new String(jsonFile.getBytes(), StandardCharsets.UTF_8);
        return getTableListFromJson(jsonStr);
    }

    public String processForContent(Map<String, Object> asMap) throws Exception {
        Context context = new Context();
        context.setVariables(asMap);
        return springTemplateEngine.process("word", context);
    }

    // 处理方案一： 同一路由下所有请求方式合并为一个表格
    private List<Table> getResult(Map<String, Object> map) throws Exception {
        //解析model
        Map<String, Model> definitionMap = parseDefinitions(map);
        //解析paths
        Map<String, Map<String, Object>> paths = JsonUtil.read(map,"$.paths");
        return processPath(paths, definitionMap);
    }

    private List<Table> processPath(Map<String, Map<String, Object>> paths, Map<String, Model> definitionMap)
            throws Exception {
        List<Table> results = new ArrayList<>();
        if (paths != null) {
            for (Map.Entry<String, Map<String, Object>> path : paths.entrySet()) {
                Iterator<Map.Entry<String, Object>> it2 = path.getValue().entrySet().iterator();
                // 1.请求路径
                String url = path.getKey();
                // 2. 循环解析每个子节点，适应同一个路径几种请求方式的场景
                while (it2.hasNext()) {
                    Map.Entry<String, Object> request = it2.next();
                    Table table = buildTable(url, definitionMap, request);
                    results.add(table);
                }
            }
        }
        return results;
    }

    private Table buildTable(String url, Map<String, Model> definitionMap, Map.Entry<String, Object> request)
            throws Exception {
        // 2. 请求方式，类似为 get,post,delete,put 这样
        String requestType = request.getKey();
        Map<String, Object> content = (Map<String, Object>) request.getValue();
        // 4. 大标题（类说明）
        String title = String.valueOf(((List) content.get("tags")).get(0));
        // 5.小标题 （方法说明）
        String tag = String.valueOf(content.get("summary"));
        // 6.接口描述
        String description = String.valueOf(content.get("summary"));
        // 7.请求参数格式，类似于 multipart/form-data
        String requestForm = "";
        // 8.返回参数格式，类似于 application/json
        String responseForm = "";
        // 9. 请求体
        List<Map<String, Object>> parameters = new ArrayList<>();
        if (swaggerVersion.startsWith("3")) {
            // 获取body参数
            HashMap<String, Object> requestBody = JsonUtil.read(content, "$.requestBody.content");
            if (requestBody != null) {
                List<String> consumes = new ArrayList<>();
                for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                    consumes.add(entry.getKey());
                    HashMap<String, Object> body = new HashMap<>();
                    body.put("name", "entity");
                    body.put("in", "body");
                    body.put("required", true);
                    String typeRef = JsonUtil.read(entry.getValue(), "$.schema.$ref");
                    if (StringUtil.isEmpty(typeRef)) {
                        typeRef = JsonUtil.read(entry.getValue(), "$.schema.type");
                        body.put("style", OBJECT);
                        if (ARRAY.equals(typeRef)) {
                            body.put("style", ARRAY);
                            LinkedHashMap<String, String> items = JsonUtil.read(entry.getValue(), "$.schema.items");
                            for (String key : items.keySet()) {
                                typeRef = items.get(key);
                            }
                        }
                    }
                    String[] split = typeRef.split("/");
                    if (ARRAY.equals(body.get("style"))) {
                        body.put("description", split[split.length - 1] + "列表集合");
                    } else {
                        body.put("description", split[split.length - 1] + "实体类");
                    }
                    body.put("type", typeRef);
                    parameters.add(body);
                }
                requestForm = StringUtil.join(consumes, ",");
            } else {
                requestForm = "application/json";
            }
            // 获取响应格式
            HashMap<String, Object> res = JsonUtil.read(content.get("responses"), "$.200.content");
            if (res != null) {
                for (Map.Entry<String, Object> entry : res.entrySet()) {
                    responseForm = entry.getKey();
                }
            } else {
                responseForm = "application/json";
            }
            // 添加公共密钥
            ArrayList<HashMap<String, List<String>>> security =
                    (ArrayList<HashMap<String, List<String>>>) content.get("security");
            if (!StringUtil.isEmpty(security)) {
                security.forEach(sec -> {
                    HashMap<String, Object> sMap = new HashMap<>();
                    for (Map.Entry<String, List<String>> ent : sec.entrySet()) {
                        Model model = definitionMap.get(ent.getKey());
                        if (!StringUtil.isEmpty(model)) {
                            sMap.put("name", ent.getKey());
                            String in = model.getIn();
                            sMap.put("in", StringUtil.isEmpty(in) ? model.getDescription() : in);
                            sMap.put("required", model.getRequire());
                            sMap.put("style", model.getType());
                            sMap.put("description",  ent.getValue() + model.getDescription());
                            sMap.put("type", model.getType());
                            parameters.add(sMap);
                        }
                    }
                });
            }
        } else {
            // swagger获取请求格式
            List<String> consumes = (List) content.get(CONSUMES);
            if (!StringUtil.isEmpty(consumes)) {
                requestForm = StringUtil.join(consumes, ",");
            }
            // 获取响应格式
            List<String> produces = (List) content.get(PRODUCES);
            if (!StringUtil.isEmpty(produces)) {
                responseForm = StringUtil.join(produces, ",");
            }
            // 添加公共密钥
            HashMap<String, HashMap<String,Object>> security = documentContext.read("$.securityDefinitions");
            if (!StringUtil.isEmpty(security)) {
                for(Map.Entry<String, HashMap<String,Object>> ent :security.entrySet()) {
                    Model model = definitionMap.get(ent.getKey());
                    HashMap<String, Object> sMap = new HashMap<>();
                    if (!StringUtil.isEmpty(model)) {
                        sMap.put("name", ent.getKey());
                        String in = model.getIn();
                        sMap.put("in", StringUtil.isEmpty(in) ? model.getDescription() : in);
                        sMap.put("required", model.getRequire());
                        sMap.put("style", model.getType());
                        sMap.put("description",  ent.getValue().get("type") + model.getDescription());
                        sMap.put("type", model.getType());
                        parameters.add(sMap);
                    }
                }
            }
        }
        // 获取请求参数
        ArrayList<Map<String, Object>> list = (ArrayList) content.get(PARAMETERS);
        if (list != null) {
            parameters.addAll(list);
        }
        // 获取响应参数 返回体
        Map<String, Object> responses = (LinkedHashMap) content.get("responses");
        //封装Table
        Table table = new Table();
        table.setTitle(title);
        table.setUrl(url);
        table.setTag(tag);
        table.setDescription(description);
        table.setRequestForm(requestForm);
        table.setResponseForm(responseForm);
        table.setRequestType(requestType);
        // 请求参数
        table.setRequestList(processRequestList(parameters, definitionMap));
        // 响应码
        table.setResponseList(processResponseCodeList(responses));
        // 取出来状态是200时的返回值
        for (Response<String> response : table.getResponseList()) {
            if (response.getCode() == 200) {
                boolean array = response.getResult().contains("===");
                Model model = definitionMap.get(definePath + response.getResult()
                        .replace("===", ""));
                if (StringUtil.isEmpty(model)) {
                    model = new Model();
                }
                if (array) {
                    model.setType("array");
                }
                table.setModel(model);
            }
        }
        // Param
        table.setRequestParam(processRequestParam(table.getRequestList()));
        table.setResponseParam(build(table.getModel()));
        return table;
    }

    /**
     * 处理请求参数列表
     */
    private List<Request> processRequestList(List<Map<String, Object>> parameters, Map<String, Model> definitionMap)
            throws Exception {
        List<Request> requestList = new ArrayList<>();
        if (CollectionUtils.isEmpty(parameters)) {
            return requestList;
        }
        for (Map<String, Object> param : parameters) {
            Request request = buildRequest(param, definitionMap);
            requestList.add(request);
        }
        return requestList;
    }

    private Request buildRequest(Map<String, Object> param, Map<String, Model> definitionMap) throws Exception {
        Request request = new Request();
        Object in = param.get("in");
        Object type = param.get("type");
        if (StringUtil.isEmpty(type)) {
            String typeRef = JsonUtil.read(param.get(SCHEMA), "$.$ref");
            if (StringUtil.isEmpty(typeRef)) {
                typeRef = JsonUtil.read(param.get(SCHEMA), "$.type");
                if (StringUtil.isEmpty(typeRef)){
                    typeRef = OBJECT;
                } else if (ARRAY.equals(typeRef)) {
                    param.put("style", ARRAY);
                    LinkedHashMap<String, String> items = JsonUtil.read(param.get(SCHEMA), "$.items");
                    for (String key : items.keySet()) {
                        type = items.get(key);
                    }
                    request.setModel(definitionMap.get(type));
                } else {
                    param.put("type",typeRef);
                    type = param.get("type");
                    request.setModel(definitionMap.get(type));
                }
            } else {
                param.put("type",typeRef);
                type = param.get("type");
                request.setModel(definitionMap.get(type));
            }
        } else {
            request.setModel(definitionMap.get(type));
        }
        Object style = param.get("style");
        String[] split = ((String)type).split("/");
        String typeName = split[split.length - 1];
        if (StringUtil.isEmpty(style)) {
            style = "header";
            request.setType(typeName);
        } else if (style.equals(ARRAY)){
            request.setType(ARRAY + ":" + typeName);
        } else {
            request.setType(typeName);
        }
        request.setStyle((String) style);
        request.setIn((String) in);
        request.setParamType(request.getIn());
        request.setName(String.valueOf(param.get("name")));
        // 是否必填
        request.setRequire((Boolean)param.get("required"));
        // 参数说明
        request.setRemark(String.valueOf(param.get(DESCRIPTION)));
        request.setDescription(request.getRemark());
        return request;
    }

    /**
     * 处理返回码列表
     * 全部状态码返回对象
     */
    private List<Response<String>> processResponseCodeList(Map<String, Object> responses) throws Exception {
        List<Response<String>> responseList = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> resIt = responses.entrySet().iterator();
        while (resIt.hasNext()) {
            Map.Entry<String, Object> entry = resIt.next();
            Response<String> response = new Response<>();
            // 状态码 200 201 401 403 404 这样
            response.setCode(Integer.parseInt(entry.getKey()));
            LinkedHashMap<String, Object> statusCodeInfo = (LinkedHashMap) entry.getValue();
            response.setMessage((String) statusCodeInfo.get(DESCRIPTION));
            Object schema = statusCodeInfo.get(SCHEMA);
            String typeRef;
            if (schema != null) {
                typeRef = JsonUtil.read(schema, "$.$ref");
                if (StringUtil.isEmpty(typeRef)) {
                    typeRef = JsonUtil.read(schema, "$.type");
                    if (ARRAY.equals(typeRef)) {
                        LinkedHashMap<String, String> items = JsonUtil.read(schema, ITEMS);
                        for (String key : items.keySet()) {
                            typeRef = "===" + items.get(key);
                        }
                    }
                }
            } else {
                List<String> list = JsonUtil.read(entry.getValue(), "$..schema.$ref");
                if (StringUtil.isEmpty(list)) {
                    list = JsonUtil.read(entry.getValue(), "$..schema.type");
                    if (!StringUtil.isEmpty(list)) {
                        typeRef = list.get(0);
                        if (ARRAY.equals(typeRef)) {
                            LinkedHashMap<String, String> items = JsonUtil.read(schema, ITEMS);
                            for (String key : items.keySet()) {
                                typeRef = "===" + items.get(key);
                            }
                        }
                    } else {
                        typeRef = OBJECT;
                    }
                } else {
                    typeRef = list.get(0);
                }
            }
            response.setResult(typeRef == null ? "" : typeRef.replace(definePath, ""));
            responseList.add(response);
        }
        return responseList;
    }

    /**
     * 解析Definition
     */
    private Map<String, Model> parseDefinitions(Map<String, Object> map) throws Exception {
        documentContext = JsonUtil.parse(map);
        Map<String, Object> definitions = documentContext.read("$.components.schemas");
        HashMap<String, HashMap<String,Object>> securitySchemes = new HashMap<>();
        if (StringUtil.isEmpty(definitions)) {
            definitions = documentContext.read("$.definitions");
            definePath = "#/definitions/";
            securitySchemes = documentContext.read("$.securityDefinitions");
            swaggerVersion = documentContext.read("$.swagger");
        } else {
            definePath = "#/components/schemas/";
            securitySchemes = documentContext.read("$.components.securitySchemes");
            swaggerVersion = documentContext.read("$.openapi");
        }
        Map<String, Model> definitionMap = new HashMap<>(256);
        if (definitions != null) {
            Iterator<String> modelNameIt = definitions.keySet().iterator();
            while (modelNameIt.hasNext()) {
                String modeName = modelNameIt.next();
                putModelFromModel(definitions, definitionMap, modeName);
            }
        }
        if (!StringUtil.isEmpty(securitySchemes)) {
            putModelAttFromSecurity(securitySchemes,definitionMap);
        }
        return definitionMap;
    }

    private void putModelAttFromSecurity(HashMap<String, HashMap<String,Object>> securitySchemes,
                                         Map<String, Model> definitionMap) throws IOException {
        // 添加公共密钥
        if (!StringUtil.isEmpty(securitySchemes)) {
            for(Map.Entry<String, HashMap<String,Object>> ent :securitySchemes.entrySet()) {
                Model model = new Model();
                model.setName(ent.getKey());
                HashMap<String, Object> sMap = new HashMap<>();
                if (!StringUtil.isEmpty(model)) {
                    HashMap<String,String> schema = JsonUtil.convertValue(ent.getValue(), HashMap.class);
                    if (!StringUtil.isEmpty(schema)) {
                        model.setIn(schema.get("in"));
                        model.setClassName(schema.get("title"));
                        model.setDescription(schema.getOrDefault("description","") + "默认授权密钥");
                        model.setRequire(Boolean.valueOf(schema.get("required")));
                        model.setType(schema.get("type"));
                    }
                    definitionMap.put(ent.getKey(),model);
                }
            }
        }
    }

    /**
     * 递归生成ModelAttr 对$ref类型设置具体属性
     */
    private Model putModelFromModel(Map<String, Object> definitions, Map<String, Model> definitionMap,
                                    String modeName) throws Exception {
        Model model;
        if ((model = definitionMap.get(definePath + modeName)) == null) {
            model = new Model();
            definitionMap.put(definePath + modeName, model);
        } else if (model.isCompleted()) {
            return definitionMap.get(definePath + modeName);
        }
        model.setName(modeName);
        model.setIn("model");
        model.setClassName(JsonUtil.read(definitions.get(modeName),"$.title"));
        model.setDescription(JsonUtil.read(definitions.get(modeName),"$.description"));
        model.setType(JsonUtil.read(definitions.get(modeName),"$.type"));
        Object requiredObject = JsonUtil.read(definitions.get(modeName), "$.required");
        List<String> requiredProperties = null;
        if (requiredObject instanceof Boolean) {
            model.setRequire((Boolean) requiredObject);
        } else {
            // 必填属性
            requiredProperties = (List<String>) requiredObject;
        }
        Map<String, Object> modeProperties = JsonUtil.read(definitions.get(modeName),"$.properties");
        List<Model> attrList = new ArrayList<>();
        if (!StringUtil.isEmpty(modeProperties)) {
            Iterator<Map.Entry<String, Object>> mIt = modeProperties.entrySet().iterator();
            //解析属性
            while (mIt.hasNext()) {
                Map.Entry<String, Object> mEntry = mIt.next();
                Map<String, Object> attrInfoMap = (Map<String, Object>) mEntry.getValue();
                Model child = new Model();
                child.setName(mEntry.getKey());
                if (!StringUtil.isEmpty(requiredProperties) && requiredProperties.contains(child.getName())) {
                    child.setRequire(true);
                }
                if (swaggerVersion.startsWith("3")) {
                    child.setType((String) attrInfoMap.get("example"));
                } else {
                    child.setType((String) attrInfoMap.get("type"));
                }
                if (attrInfoMap.get(FORMAT) != null) {
                    Object fo = attrInfoMap.get(FORMAT);
                    if (!StringUtil.isEmpty(fo)) {
                        child.setType(child.getType() + "(" + fo + ")");
                    }
                }
                Object ref = attrInfoMap.get("$ref");
                Object items = attrInfoMap.get(ITEMS);
                if (!StringUtil.isEmpty(ref) ||
                        (!StringUtil.isEmpty(items) && (!StringUtil.isEmpty(ref=((Map)items).get("$ref"))))) {
                    String refName = (String) ref;
                    //截取 #/definitions/ 后面的
                    String clsName = refName.substring(definePath.length());
                    if (definitionMap.containsKey(refName)) {
                        model.setCompleted(true);
                    }
                    Model refModel = putModelFromModel(definitions, definitionMap, clsName);
                    if (refModel != null) {
                        child.setProperties(refModel.getProperties());
                    }
                    child.setType(clsName);
                }
                child.setDescription((String) attrInfoMap.get(DESCRIPTION));
                attrList.add(child);
            }
        }
        model.setProperties(attrList);
        return model;
    }

    private String build(Model model) throws JsonProcessingException {
        if (model != null && !CollectionUtils.isEmpty(model.getProperties())) {
            Map<String, Object> responseMap = new HashMap<>();
            for (Model subModel : model.getProperties()) {
                responseMap.put(subModel.getName(), getValue(model.getType(), subModel));
            }
            return JsonUtil.writeJsonStr(responseMap);
        }
        return null;
    }

    /**
     * 封装请求体
     * @param list
     * @return
     */
    private String processRequestParam(List<Request> list) throws Exception {
        Map<String, Object> headerMap = new LinkedHashMap<>();
        Map<String, Object> queryMap = new LinkedHashMap<>();
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(list)) {
            for (Request request : list) {
                String name = request.getName();
                String paramType = request.getParamType();
                Object value = getValue(request.getStyle(), request.getModel());
                switch (paramType) {
                    case "query":
                        queryMap.put(name, value);
                        break;
                    case "header":
                        headerMap.put(name, value);
                        break;
                    case "body":
                    case "file":
                    default:
                        jsonMap.put(name, value);
                        break;
                }
            }
        }
        StringBuilder res = new StringBuilder();
        if (!StringUtil.isEmpty(queryMap)) {
            res.append(getUrlParamsByMap(queryMap));
        }
        if (!StringUtil.isEmpty(headerMap)) {
            res.append(" ").append(getHeaderByMap(headerMap));
        }
        if (!StringUtil.isEmpty(jsonMap)) {
            if (jsonMap.size() == 1) {
                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    res.append(" -d \"" + JsonUtil.writeJsonStr(entry.getValue()) + "\" ");
                }
            } else {
                res.append(" -d \"" + JsonUtil.writeJsonStr(jsonMap) + "\"");
            }
        }
        return res.toString();
    }

    /**
     * 例子中，字段的默认值
     * @param type 对应request的style model的type
     * @param model 引用的类型
     */
    private Object getValue(String type, Model model) {
        int pos;
        if ((pos = type.indexOf(":")) != -1) {
            type = type.substring(0, pos);
        }
        switch (type) {
            case "string":
                return "string";
            case "string(date-time)":
                return TimeUtil.obtainCurrentTime();
            case "integer":
            case "integer(int64)":
            case "integer(int32)":
                return 0;
            case "number":
                return 0.0;
            case "boolean":
                return true;
            case "file":
                return "(binary)";
            case ARRAY:
                List<Map<String, Object>> list = new ArrayList<>();
                Map<String, Object> map = new LinkedHashMap<>();
                if (model != null && !CollectionUtils.isEmpty(model.getProperties())) {
                    for (Model subModel : model.getProperties()) {
                        map.put(subModel.getName(), getValue(subModel.getType(), subModel));
                    }
                }
                list.add(map);
                return list;
            case OBJECT:
                map = new LinkedHashMap<>();
                if (model != null && !CollectionUtils.isEmpty(model.getProperties())) {
                    for (Model subModel : model.getProperties()) {
                        map.put(subModel.getName(), getValue(subModel.getType(), subModel));
                    }
                }
                return map;
            default:
                return type;
        }
    }
}