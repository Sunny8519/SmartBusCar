package test.yang.com.smartbuscar.module.bean;


/**
 * @Author: NiYang
 * @Date: 2017/4/9.
 */
public class UserInfo {
    private Long userId;

    private String username;

    private String password;

    private String salt;

    private String name;

    private Integer gender;

    private Long birthday;

    private String phone;

    private String email;

    private Boolean status;

    public UserInfo() {
    }

    public UserInfo(Long userId, String username, String password, String salt, String name, Integer gender, Long birthday, String phone, String email, Boolean status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.name = name;
        this.gender = gender;
        this.birthday = birthday;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Long getBirthday() {
        return birthday;
    }

    public void setBirthday(Long birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
