package com.khazhimetov.library.service.impl;

import com.khazhimetov.library.dao.UserDao;
import com.khazhimetov.library.exception.InactiveAccountException;
import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.model.User;
import com.khazhimetov.library.model.UserRole;
import com.khazhimetov.library.service.PasswordEncoder;
import com.khazhimetov.library.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer implementation for managing {@link User} operations.
 * Handles business logic related to user registration, authentication,
 * updates, deletion, and account activation.
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    /** {@inheritDoc} */
    @Override
    public User register(User user) {
        if (userDao.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("Attempted to register existing user: {}", user.getEmail());
            throw new ServiceException("User with email " + user.getEmail() + " already exists");
        }

        user.setPassword(PasswordEncoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole(UserRole.READER);
        }
        user.setActive(true);

        logger.info("Registering new user: {}", user.getEmail());
        return userDao.save(user);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = userDao.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!user.isActive()) {
                logger.warn("Authentication failed for inactive user: {}", email);
                throw new InactiveAccountException();
            }

            if (PasswordEncoder.matches(password, user.getPassword())) {
                logger.info("User authenticated successfully: {}", email);
                return Optional.of(user);
            }
        }

        logger.warn("Authentication failed for user: {}", email);
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        return userDao.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<User> findAll() {
        logger.debug("Retrieving all users");
        return userDao.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public User update(User user) {
        Optional<User> existingUser = userDao.findById(user.getId());
        if (existingUser.isEmpty()) {
            logger.warn("Attempted to update non-existent user with id: {}", user.getId());
            throw new ServiceException("User not found with id: " + user.getId());
        }

        // Don't update password if not provided
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.get().getPassword());
        } else {
            user.setPassword(PasswordEncoder.encode(user.getPassword()));
        }

        logger.info("Updating user: {}", user.getEmail());
        return userDao.update(user);
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(Long id) {
        logger.info("Deleting user with id: {}", id);
        return userDao.deleteById(id);
    }

    /** {@inheritDoc} */
    @Override
    public User toggleActiveStatus(Long id) {
        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isEmpty()) {
            logger.warn("Attempted to toggle status for non-existent user with id: {}", id);
            throw new ServiceException("User not found with id: " + id);
        }

        User user = userOpt.get();
        user.setActive(!user.isActive());

        logger.info("Toggling active status for user: {}", user.getEmail());
        return userDao.update(user);
    }
}
