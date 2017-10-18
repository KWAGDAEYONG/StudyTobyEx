package service;

import dao.UserDao;
import domain.Level;
import domain.User;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

import java.util.List;


public class UserService{
    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
    public static final int MIN_RECCOMEND_FOR_GOLD = 30;
    UserDao userDao;
    UserLevelUpgradePolicy policy;
    private DataSource dataSource;
    private PlatformTransactionManager transactionManager;
    private MailSender mailSender;

    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public void setUserDao(UserDao userDao){
        this.userDao = userDao;
    }

    public void setPolicy(UserLevelUpgradePolicy policy) {
        this.policy = policy;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void upgradeLevels() throws Exception{
        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{
            List<User> users = userDao.getAll();
            for(User user : users){
                if(policy.canUpgradeLevel(user)){
                    upgradeLevel(user);
                }
            }
            this.transactionManager.commit(status);
        } catch (Exception e){
            this.transactionManager.rollback(status);
            throw e;
        }

    }

    public void add(User user){
        if(user.getLevel() ==null){
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }

    //테스트를 위해 임시로 추상화 DI 중복 코딩(테스트에서 오버라이드 하기 위해.. 책에서 추상화 하래서 했더만 테스트를 이렇게..!)
    public void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEmail(user);
    }

    private void sendUpgradeEmail(User user){

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("useradmin@ksug.org");
        mailMessage.setSubject("Upgrade 안내");
        mailMessage.setText("사용자님의 등급이 "+user.getLevel().name()+"로 업그레이드 되었습니다.");

        this.mailSender.send(mailMessage);

    }

}
