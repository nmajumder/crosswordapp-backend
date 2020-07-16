package com.crosswordapp.dao;

import com.crosswordapp.rep.UserCommentRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class CommentDAO {
    private static Logger logger = LoggerFactory.getLogger(CommentDAO.class);

    @Value("${postgres.url}")
    private String DB_URL;
    @Value("${postgres.user}")
    private String DB_USER;
    @Value("${postgres.password}")
    private String DB_PASS;

    private final static String USER_COL = "user_id";
    private final static String TYPE_COL = "comment_type";
    private final static String TEXT_COL = "comment_text";

    private final static String POST_COMMENT =
            "INSERT INTO COMMENTS (" + USER_COL + ", " + TYPE_COL + ", " + TEXT_COL + ") VALUES (?, ?, ?)";

    public void postComment(UserCommentRep commentRep) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(POST_COMMENT)) {
            ps.setString(1, commentRep.userId);
            ps.setString(2, commentRep.type);
            ps.setString(3, commentRep.text);
            ps.execute();
            logger.info("Successfully inserted comment by user " + commentRep.userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to post comment for user: " + commentRep.userId, e);
        }
    }

}
