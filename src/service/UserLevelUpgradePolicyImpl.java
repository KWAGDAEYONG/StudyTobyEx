package service;

import dao.UserDao;
import domain.Level;
import domain.User;

import static service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static service.UserService.MIN_RECCOMEND_FOR_GOLD;

public class UserLevelUpgradePolicyImpl implements UserLevelUpgradePolicy {

    UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean canUpgradeLevel(User user) {
        Level currentLevel = user.getLevel();
        switch (currentLevel){
            case BASIC: return (user.getLogin()>=MIN_LOGCOUNT_FOR_SILVER);
            case SILVER: return (user.getRecommend()>=MIN_RECCOMEND_FOR_GOLD);
            case GOLD: return false;
            default: throw new IllegalArgumentException("Unknown Level:"+currentLevel);
        }
    }

    @Override
    public void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
    }
}
