package configure;

import dao.UserDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import com.mysql.jdbc.Driver;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import service.*;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "dao")
@ComponentScan(basePackages = "service")
@EnableSqlService
@PropertySource("/database.properties")
public class AppContext {

    @Autowired
    UserDao userDao;

    @Autowired
    UserService userService;

    @Autowired
    UserLevelUpgradePolicy userLevelUpgradePolicy;


    @Value("${db.driverClass")Class<? extends Driver> driverClass;
    @Value("${db.url") String url;
    @Value("${db.username") String username;
    @Value("${db.password") String password;

    @Bean
    public DataSource dataSource(){
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();


        dataSource.setDriverClass(this.driverClass);
        dataSource.setUrl(this.url);
        dataSource.setUsername(this.username);
        dataSource.setPassword(this.password);

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(){
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataSource());
        return tm;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Configuration
    @ComponentScan
    @Profile("test")
    public static class TestAppContext {

        @Autowired
        UserDao userDao;

        @Autowired
        UserLevelUpgradePolicy userLevelUpgradePolicy;

        @Bean
        public MailSender mailSender(){
            return new DummyMailSender();
        }

        @Bean
        public UserService testUserService(){
            return new UserServiceTest.TestUserServiceImpl();
        }
    }

    @Configuration
    @Profile("production")
    public static class ProductionAppContext {
        @Bean
        public MailSender mailSender(){
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("localhost");
            return mailSender;
        }
    }


}
