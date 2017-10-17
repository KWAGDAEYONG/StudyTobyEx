package service;

import dao.UserDao;
import dao.UserDaoJdbc;
import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {
    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    List<User> users;

    @Before
    public void setUp(){
        users = Arrays.asList(
                new User("tester1", "곽대용","p1", Level.BASIC, 49, 0),
                new User("tester2", "하해리","p2", Level.BASIC, 50, 0),
                new User("tester3", "곽명길","p3", Level.SILVER, 60, 29),
                new User("tester4", "김윤희","p4", Level.SILVER, 49, 30),
                new User("tester5", "곽대호","p5", Level.GOLD, 100, 100)
        );
    }
    @Test
    public void bean(){
        assertThat(this.userService, is(notNullValue()));
        assertThat(this.userDao, is(notNullValue()));
    }

    @Test
    public void upgradeLevels(){
        userDao.deleteAll();
        for(User user : users){
            userDao.add(user);
        }

        userService.upgradeLevels();

        checkLevel(users.get(0),Level.BASIC);
        checkLevel(users.get(1),Level.SILVER);
        checkLevel(users.get(2),Level.SILVER);
        checkLevel(users.get(3),Level.GOLD);
        checkLevel(users.get(4),Level.GOLD);

    }

    @Test
    public void add(){

        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithOutLevel = users.get(0);
        userWithOutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithOutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithOutLevel.getId());

        assertThat(userWithLevelRead.getLevel(),is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(),is(Level.BASIC));
    }

    public void checkLevel(User user, Level expectedLevel){
        User userUpdate = userDao.get(user.getId());
        assertThat(userUpdate.getLevel(),is(expectedLevel));
    }
}
