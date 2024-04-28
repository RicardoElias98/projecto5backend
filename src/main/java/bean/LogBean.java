package bean;



import dao.UserDao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

@Named
@ApplicationScoped
public class LogBean {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(UserBean.class);
    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

    @Inject
    UserDao userDao;


    @Context
    public void setHttpServletRequest(HttpServletRequest request) {
        requestHolder.set(request);
    }

    public void logUserInfo(String token, String action, int type) {

        String username = userDao.findUserByToken(token).getUsername();


        HttpServletRequest request = requestHolder.get();
        String ipAddressReq = request != null ? request.getRemoteAddr() : "localhost";
        System.out.println("IP Address: " + ipAddressReq);

        ThreadContext.put("username", username);
        ThreadContext.put("ipAddress", ipAddressReq);

        switch (type){
            case 1:
                logger.info(action);
                break;
            case 2:
                logger.error(action);
                break;
            case 3:
                logger.warn(action);
                break;
            default:
                break;
        }
    }
}
