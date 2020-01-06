package com.focus.cos.web.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class BeanFactory
{
    //Spring Bean 工厂对象.
    private static BeanFactory beanFactory = null;

    //应用程序上下文对象
    private ApplicationContext context = null;

    private BeanFactory(ApplicationContext context)
    {
        if(context == null)
        {
        	String[] configPath = {"spring/applicationContext.xml","spring/applicationContext-action.xml","spring/applicationContext-dataSource.xml","spring/applicationContext-service.xml","spring/applicationContext-webservice.xml"};
            this.context = new ClassPathXmlApplicationContext(configPath);
        }
        else
        {
            this.context = context;
        }
    }

    /**
     * 获取Bean工厂对象
     * @param context
     * @return BeanFactory
     */
    public static BeanFactory getInstance(ApplicationContext context)
    {
        if (beanFactory == null)
        {
            beanFactory = new BeanFactory(context);
        }
        return beanFactory;
    }

    /**
     * 获取Bean工厂对象.
     * @param context 应用程序上下文.
     * @return BeanFactory
     */
    public static BeanFactory getInstance()
    {
        return getInstance(null);
    }
    
    /**
     * 通过bean的ID获取到Spring管理的Bean对象
     * @param beanId
     * @return Object
     */
    public Object getBean(String beanId)
    {
        return context.getBean(beanId);
    }
}
