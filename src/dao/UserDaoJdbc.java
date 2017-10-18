package dao;



import domain.Level;
import domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class UserDaoJdbc implements UserDao {

    private JdbcTemplate jdbcTemplate;

    private RowMapper<User> userMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            user.setId(resultSet.getString("id"));
            user.setName(resultSet.getString("name"));
            user.setPassword(resultSet.getString("password"));
            user.setLevel(Level.valueOf(resultSet.getInt("level")));
            user.setLogin(resultSet.getInt("login"));
            user.setRecommend(resultSet.getInt("recommend"));
            user.setEmail(resultSet.getString("email"));

            return user;
        }
    };

    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public void add(final User user) {
        this.jdbcTemplate.update("insert into users(id,name,password, level, login, recommend, email) VALUES (?,?,?,?,?,?,?)",user.getId(),user.getName(),user.getPassword(),user.getLevel().intValue(),user.getLogin(),user.getRecommend(),user.getEmail());
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject("select * from users where id=?", new Object[]{id}, this.userMapper);
    }

    public void deleteAll(){
        this.jdbcTemplate.update("delete from users");
    }

    public int getCount(){
        return this.jdbcTemplate.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                return connection.prepareStatement("select count(*) from users");
            }
        },new ResultSetExtractor<Integer>(){
            public Integer extractData(ResultSet rs) throws SQLException,DataAccessException{
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    public List<User> getAll(){
        return this.jdbcTemplate.query("select * from users order by id", this.userMapper);
    }

    public void update(User user){
        this.jdbcTemplate.update("update users set name=?, password=?, level=?,login=?,recommend=?,email=? where id=?", user.getName(), user.getPassword(), user.getLevel().intValue(), user.getLogin(), user.getRecommend(), user.getEmail(),user.getId());

    }

}
