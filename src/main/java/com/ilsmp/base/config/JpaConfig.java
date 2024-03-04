package com.ilsmp.base.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ilsmp.base.util.JsonUtil;
import com.ilsmp.base.util.ServletUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author: ZJH Title: UserAuditor Package com.zhihui.gongdi.config Description: 启动类上增加注释：@EnableJpaAuditing JPA Audit说明
 * Date 2020/3/30 13:45
 */
@Slf4j
@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = {"${spring.base.entity-scan:com.*.*}"})
@SuppressWarnings("SpringJavaAutowiringInspection")
public class JpaConfig implements AuditorAware<Object> {

    @Value("${spring.base.user-id:user-id}")
    private String userId;

    /**
     * 获取当前创建或修改的用户
     *
     * @return 当前用户名
     */
    @SneakyThrows
    @Override
    public Optional<Object> getCurrentAuditor() {
        return Optional.of(ServletUtil.getRequestObject(userId));
    }

    /*
     * Author: zhangjiahao04
     * Description: 处理nativeQuery等于true时只能使用接口类的get方法接收参数，使用实体类接收异常问题
     * Date: 2022/3/1 00:26
     * Param:
     * return:
     **/
    @PostConstruct
    public void init() {
        GenericConversionService genericConversionService = ((GenericConversionService) DefaultConversionService.getSharedInstance());
        genericConversionService.addConverter(new JpaConvert());
    }

    static class JpaConvert implements GenericConverter {
        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new ConvertiblePair(Map.class, Object.class));
        }

        @SneakyThrows
        @Override
        public Object convert(Object source, @SuppressWarnings("NullableProblems") TypeDescriptor sourceType, TypeDescriptor targetType) {
            return JsonUtil.convert_value(source, targetType.getType());
        }
    }

    @Schema(title = "分页", description = "分页参数说明")
    @Data
    static class Page {
        @Schema(description = "上传版本号,更新数据时必传", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true,
                type = "int", example = "0", defaultValue = "0")
        @Parameter(description = "第几页，从0开始，默认为第0页", example = "0", allowEmptyValue = true, required = false)
        private Integer page;

        @Schema(description = "每一页的大小，默认为10", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true,
                type = "int", example = "0", defaultValue = "0")
        @Parameter(description = "每一页的大小，默认为10", example = "10", allowEmptyValue = true, required = false)
        private Integer size;

        @Schema(description = "按属性排序,按括号内格式填写:(属性,asc|desc)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true,
                type = "List", example = "[\"id,desc\"]", defaultValue = "[\"id,desc\"]")
        @Parameter(description = "每一页的大小，默认为10", example = "0", allowEmptyValue = true, required = false)
        private List<String> sort;
    }

    static class PageJacksonModule extends Module {
        public PageJacksonModule() {
        }

        public String getModuleName() {
            return "PageJacksonModule";
        }

        public Version version() {
            return new Version(0, 1, 0, "", (String) null, (String) null);
        }

        public void setupModule(SetupContext context) {
            context.setMixInAnnotations(org.springframework.data.domain.Page.class, PageJacksonModule.PageMixIn.class);
        }

        @JsonDeserialize(
                as = PageJacksonModule.SimplePageImpl.class
        )
        @JsonIgnoreProperties(
                ignoreUnknown = true
        )
        private interface PageMixIn {
        }

        static class SimplePageImpl<T> implements org.springframework.data.domain.Page<T> {
            private final org.springframework.data.domain.Page<T> delegate;

            SimplePageImpl(@JsonProperty("content") List<T> content, @JsonProperty("number") int number, @JsonProperty("size") int size, @JsonProperty("totalElements") @JsonAlias({"total-elements", "total_elements", "totalelements", "TotalElements"}) long totalElements, @JsonProperty("sort") Sort sort) {
                if (size > 0) {
                    PageRequest pageRequest;
                    if (sort != null) {
                        pageRequest = PageRequest.of(number, size, sort);
                    } else {
                        pageRequest = PageRequest.of(number, size);
                    }
                    this.delegate = new PageImpl<>(content, pageRequest, totalElements);
                } else {
                    this.delegate = new PageImpl<>(content);
                }
            }

            @JsonProperty
            public int getTotalPages() {
                return this.delegate.getTotalPages();
            }

            @JsonProperty
            public long getTotalElements() {
                return this.delegate.getTotalElements();
            }

            @JsonIgnore
            public <S> org.springframework.data.domain.Page<S> map(Function<? super T, ? extends S> converter) {
                return this.delegate.map(converter);
            }

            @JsonProperty
            public int getNumber() {
                return this.delegate.getNumber();
            }

            @JsonProperty
            public int getSize() {
                return this.delegate.getSize();
            }

            @JsonProperty
            public int getNumberOfElements() {
                return this.delegate.getNumberOfElements();
            }

            @JsonProperty
            public List<T> getContent() {
                return this.delegate.getContent();
            }

            @JsonProperty
            public boolean hasContent() {
                return this.delegate.hasContent();
            }

            @JsonIgnore
            public Sort getSort() {
                return this.delegate.getSort();
            }

            @JsonProperty
            public boolean isFirst() {
                return this.delegate.isFirst();
            }

            @JsonProperty
            public boolean isLast() {
                return this.delegate.isLast();
            }

            @JsonIgnore
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            @JsonIgnore
            public boolean hasPrevious() {
                return this.delegate.hasPrevious();
            }

            @JsonIgnore
            public Pageable getPageable() {
                return this.delegate.getPageable();
            }

            @JsonIgnore
            public Pageable nextPageable() {
                return this.delegate.nextPageable();
            }

            @JsonIgnore
            public Pageable previousPageable() {
                return this.delegate.previousPageable();
            }

            @JsonIgnore
            public Iterator<T> iterator() {
                return this.delegate.iterator();
            }

            @JsonIgnore
            public boolean isEmpty() {
                return this.delegate.isEmpty();
            }
        }
    }

    static class SortJacksonModule extends Module {
        public SortJacksonModule() {
        }

        public String getModuleName() {
            return "SortModule";
        }

        public Version version() {
            return new Version(0, 1, 0, "", (String) null, (String) null);
        }

        public void setupModule(SetupContext context) {
            SimpleSerializers serializers = new SimpleSerializers();
            serializers.addSerializer(Sort.class, new SortSerializer());
            context.addSerializers(serializers);
            SimpleDeserializers deserializers = new SimpleDeserializers();
            deserializers.addDeserializer(Sort.class, new SortDeserializer());
            context.addDeserializers(deserializers);
        }
    }

    public static class SortDeserializer extends JsonDeserializer<Sort> {
        public SortDeserializer() {
        }

        public Sort deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
            if (!treeNode.isArray()) {
                return null;
            } else {
                ArrayNode arrayNode = (ArrayNode) treeNode;
                List<Sort.Order> orders = new ArrayList();
                Iterator var6 = arrayNode.iterator();
                while (var6.hasNext()) {
                    JsonNode jsonNode = (JsonNode) var6.next();
                    Sort.Order order = new Sort.Order(Sort.Direction.valueOf(jsonNode.get("direction").textValue()), jsonNode.get("property").textValue());
                    orders.add(order);
                }
                return Sort.by(orders);
            }
        }

        public Class<Sort> handledType() {
            return Sort.class;
        }
    }

    public static class SortSerializer extends JsonSerializer<Sort> {
        public SortSerializer() {
        }

        public void serialize(Sort value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            value.iterator().forEachRemaining((v) -> {
                try {
                    gen.writeObject(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            gen.writeEndArray();
        }

        public Class<Sort> handledType() {
            return Sort.class;
        }
    }

    /*
     * Description: 解决feign请求page分页无法解析
     * Author: zhangjiahao04
     * Date: 2022/11/11 17:20
     **/
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({Module.class, Page.class, Sort.class})
    static class FeignJacksonConfiguration {
        @Bean
        public PageJacksonModule pageJacksonModule() {
            return new PageJacksonModule();
        }

        @Bean
        @ConditionalOnMissingBean(SortJacksonModule.class)
        public SortJacksonModule sortModule() {
            return new SortJacksonModule();
        }
    }

}