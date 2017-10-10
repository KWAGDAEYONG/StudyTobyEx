package dao;

import domain.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.sql.SQLException;

public class UserDaoTest {
    public static void main(String[]args) throws ClassNotFoundException,SQLException{

        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
/*
        ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);*/
        UserDao userDao = context.getBean("userDao", UserDao.class);
       /* User user = new User();
        user.setId("whiteship");
        user.setName("name");
        user.setPassword("married");

        userDao.add(user);

        System.out.println(user.getId() + " 추가 완료");*/

        User user2 = userDao.get("whiteship");
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + " 가져오기 완료");

        DaoFactory daoFactory = new DaoFactory();
        UserDao dao1 = daoFactory.userDao();
        UserDao dao2 = daoFactory.userDao();

        System.out.println(dao1);
        System.out.println(dao2);

        UserDao dao3 = context.getBean("userDao",UserDao.class);
        UserDao dao4 = context.getBean("userDao",UserDao.class);

        System.out.println(dao3);
        System.out.println(dao4);
    }
}
