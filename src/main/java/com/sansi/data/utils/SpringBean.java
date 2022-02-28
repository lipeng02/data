package com.sansi.data.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringBean implements ApplicationContextAware {
    // 获取spring容器，以访问容器中定义的其他bean
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // 实现ApplicationContextAware接口的回调方法，设置上下文环境
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringBean.applicationContext == null)
            SpringBean.applicationContext = applicationContext;
    }

    // 获取对象
    public static <T> T getBean(Class<T> tClass){
        return getApplicationContext().getBean(tClass);
    }
}
