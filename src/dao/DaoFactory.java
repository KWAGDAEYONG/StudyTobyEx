package dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

@Configuration
public class DaoFactory {
    @Bean
    public UserDaoJdbc userDao(){
        UserDaoJdbc userDaoJdbc = new UserDaoJdbc();
        userDaoJdbc.setDataSource(dataSource());
        return userDaoJdbc;
    }
    @Bean
    public ConnectionMaker getConnectionMaker(){
        return new DConnectionMaker();
    }
    @Bean
    public DataSource dataSource(){
        SimpleDriverDataSource driverDataSource = new SimpleDriverDataSource();

        driverDataSource.setDriverClass(com.mysql.jdbc.Driver.class);
        driverDataSource.setUrl("jdbc:mysql://localhost:3306/toby");
        driverDataSource.setUsername("root");
        driverDataSource.setPassword("rhkr1636");

        return driverDataSource;
    }
}
