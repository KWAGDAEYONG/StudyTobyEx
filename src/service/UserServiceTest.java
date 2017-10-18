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
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static service.UserService.MIN_RECCOMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {
    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    @Autowired
    DataSource dataSource;

    @Autowired
    UserLevelUpgradePolicy userLevelUpgradePolicy;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    List<User> users;

    @Before
    public void setUp(){
        users = Arrays.asList(
                new User("tester1", "곽대용","p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0,"abc@abc"),
                new User("tester2", "하해리","p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0,"abc@abc"),
                new User("tester3", "곽명길","p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD-1,"abc@abc"),
                new User("tester4", "김윤희","p4", Level.SILVER, 49, MIN_RECCOMEND_FOR_GOLD,"abc@abc"),
                new User("tester5", "곽대호","p5", Level.GOLD, 100, Integer.MAX_VALUE,"abc@abc")
        );
    }
    @Test
    public void bean(){
        assertThat(this.userService, is(notNullValue()));
        assertThat(this.userDao, is(notNullValue()));
    }

    @Test
    @DirtiesContext
    public void upgradeLevels() throws Exception{
        userDao.deleteAll();
        for(User user : users){
            userDao.add(user);
        }
        MockMailSender mockMailSender = new MockMailSender();
        userService.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevel(users.get(0),false);
        checkLevel(users.get(1),true);
        checkLevel(users.get(2),false);
        checkLevel(users.get(3),true);
        checkLevel(users.get(4),false);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(),is(2));
        assertThat(request.get(0),is(users.get(1).getEmail()));
        assertThat(request.get(1),is(users.get(3).getEmail()));
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

    @Test
    public void upgradeAllOrNothing()throws Exception{
        UserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setDataSource(this.dataSource);
        testUserService.setPolicy(this.userLevelUpgradePolicy);
        testUserService.setTransactionManager(this.transactionManager);
        testUserService.setMailSender(this.mailSender);
        userDao.deleteAll();
        for(User user : users){
            userDao.add(user);
        }

        try{
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch (TestUserServiceException e){

        }
        checkLevel(users.get(1),false);
    }

    public void checkLevel(User user, boolean upgraded){
        User userUpdate = userDao.get(user.getId());
        if(upgraded){
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        }else{
            assertThat(userUpdate.getLevel(),is(user.getLevel()));
        }
    }

    static class TestUserService extends UserService{
        private String id;

        private TestUserService(String id){
            this.id = id;
        }

        @Override
         public void upgradeLevel(User user){
            if(user.getId().equals(this.id)){
                throw new TestUserServiceException();
            }
            super.policy.upgradeLevel(user);
        }

    }

    static class TestUserServiceException extends RuntimeException{

    }

    static class MockMailSender implements MailSender{
        private List<String> requests = new ArrayList<String>();

        public List<String> getRequests(){
            return requests;
        }

        @Override
        public void send(SimpleMailMessage simpleMailMessage) throws MailException {
            requests.add(simpleMailMessage.getTo()[0]);
        }

        @Override
        public void send(SimpleMailMessage[] mailMessages) throws MailException{

        }
    }
}
