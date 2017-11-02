package service;

import dao.UserDao;
import domain.Level;
import domain.User;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.List;

public class UserServiceImpl implements UserService {

    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
    public static final int MIN_RECCOMEND_FOR_GOLD = 30;

    UserDao userDao;
    MailSender mailSender;
    UserLevelUpgradePolicy policy;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setPolicy(UserLevelUpgradePolicy policy) {
        this.policy = policy;
    }

    public void upgradeLevels(){
        List<User> users = userDao.getAll();
        for(User user : users){
            if(policy.canUpgradeLevel(user)){
                upgradeLevel(user);
            }
        }
    }

    public void add(User user){
        if(user.getLevel() ==null){
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }

    private void sendUpgradeEmail(User user){

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("useradmin@ksug.org");
        mailMessage.setSubject("Upgrade 안내");
        mailMessage.setText("사용자님의 등급이 "+user.getLevel().name()+"로 업그레이드 되었습니다.");

        this.mailSender.send(mailMessage);

    }

    public void deleteAll(){
        userDao.deleteAll();
    }

    public User get(String id){
        return userDao.get(id);
    }

    public List<User> getAll(){
        return userDao.getAll();
    }

    public void update(User user){
        userDao.update(user);
    }

    //테스트를 위해 임시로 추상화 DI 중복 코딩(테스트에서 오버라이드 하기 위해.. 책에서 추상화 하래서 했더만 테스트를 이렇게..!)
    public void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEmail(user);
    }
}
