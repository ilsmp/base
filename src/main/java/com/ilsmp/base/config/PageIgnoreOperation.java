//package com.ilsmp.base.config;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import io.swagger.v3.oas.models.Operation;
//import io.swagger.v3.oas.models.parameters.Parameter;
//import org.springdoc.core.customizers.GlobalOperationCustomizer;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Component;
//import org.springframework.web.method.HandlerMethod;
//
///**
// * Description: 忽略参数 Package: com.ilsmp.base.config Author: zhangjiahao04 Title: PageIgnoreOperation Date:
// * 2024/3/3 19:27
// */
//
//@Component
//public class PageIgnoreOperation implements GlobalOperationCustomizer {
//
//    public static final String IGNORE_PARAMETER_EXTENSION_NAME = "x-ignoreParameters";
//
//    private static final String[] IGNORE = new String[]{"countId", "maxLimit", "optimizeCountSql", "pages", "records", "searchCount", "total"};
//
//    @Override
//    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
//        if (hasPage(operation)) {
//            addExtensionParameters(IGNORE, operation);
//        }
//        return null;
//    }
//
//    private boolean hasPage(Operation operation) {
//        for (Parameter parameter : operation.getParameters()) {
//            Class<? extends Parameter> aClass = parameter.getClass();
//            if (aClass.isAssignableFrom(Pageable.class)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void addExtensionParameters(String[] params, Operation operation) {
//        if (params != null && params.length > 0) {
//            List<Parameter> parameters = new ArrayList<>();
//            Map<String, Boolean> map = new HashMap<>(8);
//            for (String ignore : params) {
//                if (ignore != null && !ignore.isEmpty() && !"null".equals(ignore)) {
//                    Parameter para = new Parameter();
//                    para.setDeprecated(true);
//                    para.setName(ignore);
//                    parameters.add(para);
//                }
//            }
//            operation.parameters(parameters);
//        }
//    }
//
//}
//
