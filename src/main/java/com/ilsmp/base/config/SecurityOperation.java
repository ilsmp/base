package com.ilsmp.base.config;

import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Description: Spring Security 注解展示 Package: com.ilsmp.base.config Author: zhangjiahao04 Title:
 * SecurityOperation Date: 2024/3/3 19:51
 */

@Component
@ConditionalOnDefaultWebSecurity
public class SecurityOperation implements GlobalOperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        StringBuilder notesBuilder = new StringBuilder(operation.getDescription() == null ? "" : operation.getDescription());
        getClassAnnotationNote(notesBuilder, handlerMethod);
        getMethodAnnotationNote(notesBuilder, handlerMethod);
        operation.setDescription(notesBuilder.toString());
        return operation;
    }

    private void getClassAnnotationNote(StringBuilder notesBuilder, HandlerMethod handlerMethod) {
        List<String> values = new ArrayList<>();
        PostAuthorize postAuthorize = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), PostAuthorize.class);
        if (postAuthorize != null) {
            values.add(postAuthorize.value());
        }
        PostFilter postFilter = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), PostFilter.class);
        if (postFilter != null) {
            values.add(postFilter.value());
        }
        PreAuthorize preAuthorize = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), PreAuthorize.class);
        if (preAuthorize != null) {
            values.add(preAuthorize.value());
        }
        PreFilter preFilter = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), PreFilter.class);
        if (preFilter != null) {
            values.add(preFilter.value());
        }
        if (!values.isEmpty()) {
            notesBuilder.append("<p />").append("class: ").append(String.join("，", values));
        }
    }

    private void getMethodAnnotationNote(StringBuilder notesBuilder, HandlerMethod handlerMethod) {
        List<String> values = new ArrayList<>();
        PostAuthorize postAuthorize = handlerMethod.getMethodAnnotation(PostAuthorize.class);
        if (postAuthorize != null) {
            values.add(postAuthorize.value());
        }
        PostFilter postFilter = handlerMethod.getMethodAnnotation(PostFilter.class);
        if (postFilter != null) {
            values.add(postFilter.value());
        }
        PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);
        if (preAuthorize != null) {
            values.add(preAuthorize.value());
        }
        PreFilter preFilter = handlerMethod.getMethodAnnotation(PreFilter.class);
        if (preFilter != null) {
            values.add(preFilter.value());
        }
        if (!values.isEmpty()) {
            notesBuilder.append("<p />").append("method: ").append(String.join("，", values));
        }
    }
}
