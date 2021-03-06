package configure;

import com.mysql.jdbc.Driver;
import dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
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

    @Autowired
    Environment env;

    @Bean
    public DataSource dataSource(){
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

       /* try{
            dataSource.setDriverClass((Class<? extends java.sql.Driver>)Class.forName(env.getProperty("db.driverClass")));

        }catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.username"));
        dataSource.setPassword(env.getProperty("db.password"));*/
        dataSource.setDriverClass(Driver.class);
        dataSource.setUrl("jdbc:mysql://localhost:3306/toby");
        dataSource.setUsername("root");
        dataSource.setPassword("rhkr1636");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(){
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataSource());
        return tm;
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
