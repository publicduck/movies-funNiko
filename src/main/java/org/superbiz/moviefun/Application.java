package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    DatabaseServiceCredentials getDatabaseServiceCredentials (@Value("${VCAP_SERVICES}") String vcapServices){
        return new DatabaseServiceCredentials(vcapServices);
    }

    //returns a datasource for the albums
    @Bean
    DataSource albumsDataSource (DatabaseServiceCredentials getDatabaseServiceCredentials){
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(getDatabaseServiceCredentials.jdbcUrl("albums-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    //returns a datasource for the movies
    @Bean
    DataSource moviesDataSource (DatabaseServiceCredentials getDatabaseServiceCredentials){
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(getDatabaseServiceCredentials.jdbcUrl("movies-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    HibernateJpaVendorAdapter hibernateJpaVendorAdapter (){
        HibernateJpaVendorAdapter hiber = new HibernateJpaVendorAdapter();
        hiber.setDatabase(Database.MYSQL);
        hiber.setGenerateDdl(true);
        hiber.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        return hiber;
    }

    // This is the EntityManagerFactory
    @Bean
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBeanMovies(
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter, DataSource moviesDataSource)
    {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(moviesDataSource);
        bean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        bean.setPackagesToScan("org.superbiz.moviefun.movies");
        bean.setPersistenceUnitName("movies");
        return bean;
    }

    // this is the EntityManagerFactory!
    @Bean
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBeanAlbums(
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter, DataSource albumsDataSource)
    {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(albumsDataSource);
        bean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        bean.setPackagesToScan("org.superbiz.moviefun.albums");
        bean.setPersistenceUnitName("albums");
        return bean;
    }

    @Bean
    PlatformTransactionManager managerAlbums (EntityManagerFactory localContainerEntityManagerFactoryBeanAlbums){
        JpaTransactionManager transAlbums = new JpaTransactionManager();
        transAlbums.setEntityManagerFactory(localContainerEntityManagerFactoryBeanAlbums);
        return transAlbums;
    }

    @Bean
    PlatformTransactionManager managerMovies(EntityManagerFactory localContainerEntityManagerFactoryBeanMovies){
        JpaTransactionManager transMovies = new JpaTransactionManager(localContainerEntityManagerFactoryBeanMovies);
        return transMovies;
    }
}

