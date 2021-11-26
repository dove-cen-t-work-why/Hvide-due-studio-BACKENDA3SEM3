package facades;

import dtos.UserDTO;
import entities.Role;
import entities.User;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;

import security.errorhandling.AuthenticationException;
import utils.EMF_Creator;

import java.util.List;

/**
 * @author lam@cphbusiness.dk
 */
public class UserFacade {

    private static EntityManagerFactory emf;
    private static UserFacade instance;

    private UserFacade() {
    }

    /**
     *
     * @param _emf
     * @return the instance of this facade.
     */
    public static UserFacade getUserFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserFacade();
        }
        return instance;
    }

    public User getVerifiedUser(String username, String password) throws AuthenticationException {
        EntityManager em = emf.createEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
            if (user == null || !user.verifyPassword(password)) {
                throw new AuthenticationException("Invalid user name or password");
            }
        } finally {
            em.close();
        }
        return user;
    }

    public UserDTO signup(String username, String password) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, username);
        if (user != null) {
            throw new WebApplicationException("Username is already taken.", 409);
        }
        user = new User(username, password);
        Role role = new Role("user");   // this works, but I worry slightly that we don't get the actual role entity with "find"
        user.addRole(role);
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        return new UserDTO(user);
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = EMF_Creator.createEntityManagerFactory();
        UserFacade uf = UserFacade.getUserFacade(emf);
        EntityManager em = emf.createEntityManager();
        Role role = em.find(Role.class, "user");
        System.out.println(role.getUserList());
        List<User> users =em.createQuery("SELECT r.userList FROM Role r WHERE r.roleName = 'user'", User.class).getResultList();
        users.forEach(u -> System.out.println(u.getUserName()));
    }

}
