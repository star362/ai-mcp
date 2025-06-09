package com.star.ai.test.aimcp.repository;

import com.star.ai.test.aimcp.enetity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
