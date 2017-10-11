package dao;

import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations ="/test-applicationContext.xml" )
public class UserDaoTest {

    @Autowired
    private ApplicationContext context;
    private UserDao userDao;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp(){
        userDao = new UserDao();
        DataSource dataSource = new SingleConnectionDataSource("jdbc:mysql://localhost:3306/toby","root","rhkr1636",true);
        userDao.setDataSource(dataSource);
        this.user1 = new User("tester1","곽대용","123");
        this.user2 = new User("tester2","하해리","123");
        this.user3 = new User("tester3","한영준","123");
    }

    @Test
    public void addAndGet() throws ClassNotFoundException, SQLException {
        userDao.deleteAll();
        assertThat(userDao.getCount(), is(0));

        userDao.add(user1);
        userDao.add(user2);

        assertThat(userDao.getCount(),is(2));

        User target1 = userDao.get(user1.getId());
        assertThat(target1.getName(), is(user1.getName()));
        assertThat(target1.getPassword(), is(user1.getPassword()));

        User target2 = userDao.get(user2.getId());
        assertThat(target2.getName(), is(user2.getName()));
        assertThat(target2.getPassword(), is(user2.getPassword()));
    }

    @Test
    public void count() throws SQLException,ClassNotFoundException{

        userDao.deleteAll();
        assertThat(userDao.getCount(),is(0));

        userDao.add(user1);
        assertThat(userDao.getCount(),is(1));

        userDao.add(user2);
        assertThat(userDao.getCount(),is(2));

        userDao.add(user3);
        assertThat(userDao.getCount(),is(3));

    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException, ClassNotFoundException{

        userDao.deleteAll();
        assertThat(userDao.getCount(),is(0));

        userDao.get("unknown_id");
    }
}
