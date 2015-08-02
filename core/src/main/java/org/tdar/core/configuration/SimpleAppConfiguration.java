package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.processes.ProcessManager;

@EnableTransactionManagement()
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = { "org.tdar" }, excludeFilters= {})
@ImportResource(value = { "classpath:/spring-local-settings.xml" })
public class SimpleAppConfiguration implements Serializable {

	private static final long serialVersionUID = 2190713147269025044L;
	public transient Logger logger = LoggerFactory.getLogger(getClass());

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(@Qualifier("tdarMetadataDataSource") DataSource dataSource) {
        Properties properties = new Properties();

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        builder.scanPackages(new String[] { "org.tdar"});
        builder.addProperties(properties);
        return builder.buildSessionFactory();
    }

    @Bean(name = "mailSender")
    public JavaMailSender getJavaMailSender(@Value("${mail.smtp.host:localhost}") String hostname) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(hostname);
        return sender;
    }
    

    @Bean(name = "sessionData")
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SessionData getSessionData() {
        return new SessionData();
    }


    @Bean
    @Primary
    public HibernateTransactionManager transactionManager(@Qualifier("tdarMetadataDataSource") DataSource dataSource) throws PropertyVetoException {
        HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(getSessionFactory(dataSource));
        return hibernateTransactionManager;
    }


	@Bean(name="processManager")
	public ProcessManager processManager() {
		return null;
	}
}
