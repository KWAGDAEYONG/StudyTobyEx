package dao;

import domain.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;

public class UserDaoTest {
    public static void main(String[]args) throws ClassNotFoundException,SQLException{

        ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);

        UserDao userDao = context.getBean("userDao", UserDao.class);
        User user = new User();
        user.setId("whiteship");
        user.setName("name");
        user.setPassword("married");

        userDao.add(user);

        System.out.println(user.getId() + " 추가 완료");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + " 가져오기 완료");
    }
}
