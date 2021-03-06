package service;

import configure.AppContext;
import dao.UserDao;
import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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
import static org.mockito.Mockito.*;
import static service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = AppContext.class)
public class UserServiceTest {
    @Autowired
    UserService userService;

    @Autowired
    UserService testUserService;

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

    @Autowired
    ApplicationContext applicationContext;

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
    public void upgradeLevels() throws Exception{
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        MockUserDao mockUserDao = new MockUserDao(this.users);
        MockMailSender mockMailSender = new MockMailSender();


        userServiceImpl.setPolicy(userLevelUpgradePolicy);
        userServiceImpl.setUserDao(mockUserDao);
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();
        assertThat(updated.size(), is(2));

        checkUserAndLevel(updated.get(0), "tester2",Level.SILVER);
        checkUserAndLevel(updated.get(1),"tester4",Level.GOLD);

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
    @DirtiesContext
    public void upgradeAllOrNothing()throws Exception{
        userDao.deleteAll();
        for(User user : users){
            userDao.add(user);
        }

        try{
            this.testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch (TestUserServiceException e){

        }
        checkLevel(users.get(1),false);
    }

    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel){
        assertThat(updated.getId(), is(expectedId));
        assertThat(updated.getLevel(), is(expectedLevel));
    }

    public void checkLevel(User user, boolean upgraded){
        User userUpdate = userDao.get(user.getId());
        if(upgraded){
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        }else{
            assertThat(userUpdate.getLevel(),is(user.getLevel()));
        }
    }

    @Test
    public void mockUpgradeLevels() throws Exception{
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserLevelUpgradePolicy userLevelUpgradePolicy = new UserLevelUpgradePolicyImpl();

        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);
        userServiceImpl.setPolicy(userLevelUpgradePolicy);

        userServiceImpl.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(),is(Level.GOLD));
    }



    public  static class TestUserServiceImpl extends UserServiceImpl{
        private String id = "tester4";

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


    static class MockUserDao implements UserDao{
        //업그레이드 후보
        private List<User> users;
        //업그레이드 대상 오브젝트
        private List<User> updated = new ArrayList();


        private MockUserDao(List<User> users){
            this.users = users;
        }

        public List<User> getUpdated(){
            return this.updated;
        }

        public List<User> getAll(){
            return this.users;
        }

        public void update(User user){
            updated.add(user);
        }

        public void add(User user){
            throw new UnsupportedOperationException();
        }

        public void deleteAll(){
            throw new UnsupportedOperationException();
        }

        public User get(String id){
            throw new UnsupportedOperationException();
        }

        public int getCount(){
            throw new UnsupportedOperationException();
        }
    }
}
