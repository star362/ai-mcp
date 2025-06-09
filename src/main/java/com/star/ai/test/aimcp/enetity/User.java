package com.star.ai.test.aimcp.enetity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("star_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String _id;
    private String name;
    private String age;
    private String sex;
}
