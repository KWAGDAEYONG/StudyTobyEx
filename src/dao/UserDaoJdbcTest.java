package dao;

import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations ="/test-applicationContext.xml" )
public class UserDaoJdbcTest {

    @Autowired
    private UserDao userDao;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp(){
        this.user1 = new User("tester1","곽대용","123", Level.BASIC,1,0);
        this.user2 = new User("tester2","하해리","123", Level.SILVER,55,10);
        this.user3 = new User("tester3","한영준","123",Level.GOLD,100,40);
    }

    @Test
    public void addAndGet() throws ClassNotFoundException, SQLException {
        userDao.deleteAll();
        assertThat(userDao.getCount(), is(0));

        userDao.add(user1);
        userDao.add(user2);

        assertThat(userDao.getCount(),is(2));

        User target1 = userDao.get(user1.getId());
        checkSameUser(target1, user1);

        User target2 = userDao.get(user2.getId());
        checkSameUser(target2,user2);
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

    @Test
    public void getAll() throws SQLException, ClassNotFoundException{
        userDao.deleteAll();

        List<User> users0 = userDao.getAll();
        assertThat(users0.size(),is(0));

        userDao.add(user1);
        List<User> users1 = userDao.getAll();
        assertThat(users1.size(), is(1));
        checkSameUser(user1, users1.get(0));

        userDao.add(user2);
        List<User> users2 = userDao.getAll();
        assertThat(users2.size(), is(2));
        checkSameUser(user1, users2.get(0));
        checkSameUser(user2, users2.get(1));

        userDao.add(user3);
        List<User> users3 = userDao.getAll();
        assertThat(users3.size(), is(3));
        checkSameUser(user1, users3.get(0));
        checkSameUser(user2, users3.get(1));
        checkSameUser(user3, users3.get(2));

    }

    @Test
    public void update(){
        userDao.deleteAll();

        userDao.add(user1);
        userDao.add(user2);

        user1.setName("오민규");
        user1.setPassword("1234");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);

        userDao.update(user1);

        User updateUser = userDao.get(user1.getId());
        User noUpdateUser = userDao.get(user2.getId());
        checkSameUser(user1, updateUser);
        checkSameUser(user2,noUpdateUser);
    }


    private void checkSameUser(User user1, User user2){
        assertThat(user1.getId(), is(user2.getId()));
        assertThat(user1.getName(), is(user2.getName()));
        assertThat(user1.getPassword(), is(user2.getPassword()));
        assertThat(user1.getLevel(), is(user2.getLevel()));
        assertThat(user1.getLogin(), is(user2.getLogin()));
        assertThat(user1.getRecommend(), is(user2.getRecommend()));
    }
}
