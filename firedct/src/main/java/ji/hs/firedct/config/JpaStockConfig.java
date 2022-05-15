package ji.hs.firedct.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Jpa 설정
 * @author now2woy
 *
 */
@Configuration
@EnableJpaRepositories(
	basePackages = "ji.hs.firedct.data.stock", 
	entityManagerFactoryRef = "stockEntityManagerFactory", 
	transactionManagerRef = "stockTransactionManager"
)
public class JpaStockConfig {
	@Autowired
	private Environment env;
	
	@Bean
	@Primary
	public DataSource stockDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.stock.datasource.driverClassName"));
		dataSource.setUrl(env.getProperty("spring.stock.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.stock.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.stock.datasource.password"));
		
		return dataSource;
	}
	
	/**
	 * 
	 * @return
	 */
	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean stockEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(stockDataSource());
		em.setPackagesToScan(new String[] { "ji.hs.firedct.data.stock" });
		
		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(JpaProperties());
		
		return em;
	}
	
	/**
	 * 
	 * @return
	 */
	@Bean
	@Primary
	public PlatformTransactionManager stockTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(stockEntityManagerFactory().getObject());
		return transactionManager;
	}
	
	/**
	 * 
	 * @return
	 */
	@Bean
	public PersistenceExceptionTranslationPostProcessor stockExceptionTranslation(){
		return new PersistenceExceptionTranslationPostProcessor();
	}
	
	/**
	 * 
	 * @return
	 */
	private Properties JpaProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
		properties.setProperty("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
		properties.setProperty("hibernate.show_sql", env.getProperty("spring.jpa.properties.hibernate.show_sql"));
		properties.setProperty("hibernate.format_sql", env.getProperty("spring.jpa.properties.hibernate.format_sql"));
		return properties;
	}
}
