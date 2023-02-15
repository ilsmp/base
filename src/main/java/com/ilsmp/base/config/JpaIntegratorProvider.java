package com.ilsmp.base.config;

import java.util.List;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.bytecode.enhance.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.internal.DefaultMergeEventListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.internal.PropertyAccessStrategyBackRefImpl;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.type.ForeignKeyDirection;

/**
 * Description: 直接设置EventType.MERGE的listener是自定义的MergeEventListener
 * 解决save更新null值也被更新
 * Package: com.ilsmp.base.config Author: zhangjiahao04
 * Title: JpaIntegratorProvider Date: 2022/11/11 13:44
 */

public class JpaIntegratorProvider implements IntegratorProvider {

    @Override
    public List<Integrator> getIntegrators() {
        return List.of(new Integrator() {
            @Override
            public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
                EventListenerRegistry listenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
                // 直接设置EventType.MERGE的listener是自定义的JpaMergeEventListener
                listenerRegistry.setListeners(EventType.MERGE, JpaMergeEventListener.class);
            }

            @Override
            public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

            }
        });
    }

    /*
     * Description: 同上,自定义的MergeEventListener修改save更新操作
     * Author: zhangjiahao04
     * Date: 2022/11/11 17:21
     **/
    public static class JpaMergeEventListener extends DefaultMergeEventListener {
        @Override
        protected void copyValues(EntityPersister persister, Object entity, Object target, SessionImplementor source,
                                  Map copyCache) {
            var original = persister.getPropertyValues(entity);
            var tar = persister.getPropertyValues(target);
            var types = persister.getPropertyTypes();
            // 获取列是否可以为空, 即@Column中的nullable配置
            var propertyNullability = persister.getPropertyNullability();
            Object[] copied = new Object[original.length];
            for (int i = 0; i < types.length; i++) {
                // 修改此处的判断条件 验证@Column注解的nullable 下面这种用法既可以给可以为null的字段设置null 又不会更新不能为null的字段
                if (original[i] == null) {
                    // 如果nullable为false 并且传入的值为null 则使用原数据 即不更新null
                    copied[i] = tar[i];
                } else if (original[i] == LazyPropertyInitializer.UNFETCHED_PROPERTY || original[i] ==
                        PropertyAccessStrategyBackRefImpl.UNKNOWN) {
                    copied[i] = tar[i];
                } else if (tar[i] == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
                    copied[i] = types[i].replace(original[i], null, source, target, copyCache);
                } else {
                    copied[i] = types[i].replace(original[i], tar[i], source, target, copyCache);
                }
            }
            persister.setPropertyValues(target, copied);
        }

        @Override
        protected void copyValues(EntityPersister persister, Object entity, Object target, SessionImplementor source, Map copyCache, ForeignKeyDirection foreignKeyDirection) {
            super.copyValues(persister, entity, target, source, copyCache, foreignKeyDirection);
        }
    }

}
