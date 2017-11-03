package configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sqlservice.SimpleSqlService;
import sqlservice.SqlService;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SqlServiceContext {
    @Bean
    public SqlService sqlService(){
        SimpleSqlService simpleSqlService = new SimpleSqlService();
        Map<String,String> sqlMap = new HashMap<String, String>();

        sqlMap.put("userAdd","insert into users(id,name,password, level, login, recommend, email) VALUES (?,?,?,?,?,?,?)");
        sqlMap.put("userGet","select * from users where id=?");
        sqlMap.put("userGetAll","select * from users order by id");
        sqlMap.put("userDeleteAll","delete from users");
        sqlMap.put("userGetCount","select count(*) from users");
        sqlMap.put("userUpdate","update users set name=?, password=?, level=?,login=?,recommend=?,email=? where id=?");

        simpleSqlService.setSqlMap(sqlMap);
        return simpleSqlService;
    }
}
