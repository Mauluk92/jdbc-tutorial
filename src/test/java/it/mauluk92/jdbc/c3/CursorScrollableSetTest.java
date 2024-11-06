package it.mauluk92.jdbc.c3;

import it.mauluk92.jdbc.testutils.ConnectionParameterResolver;
import it.mauluk92.jdbc.testutils.SqlCallback;
import it.mauluk92.jdbc.testutils.annotation.JdbcSql;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class contains tests to validate rules about
 * Scrollable result sets
 */
@ExtendWith({ConnectionParameterResolver.class, SqlCallback.class})
public class CursorScrollableSetTest {
    /**
     * A result set can be either scrollable or non-scrollable.
     * A non-scrollable result set can only move forward and this is achieved
     * through the Result's Set constant TYPE_FORWARD_ONLY
     */
    @Test
    @JdbcSql("c3/cursor_scrollable_set/create_non_scrollable_result_set.sql")
    @DisplayName("Create non scrollable result set")
    public void createNonScrollableResultSet(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM TABLE_C3");
        rs.next();
        Assertions.assertThrows(SQLException.class, rs::previous);
    }

    /**
     * A result set can be either scrollable or non-scrollable.
     * A scrollable result set can move forward or backward and this is achieved
     * through the Result's Set constants TYPE_SCROLL_INSENSITIVE or TYPE_SCROLL_SENSITIVE
     */
    @Test
    @JdbcSql("c3/cursor_scrollable_set/create_scrollable_result_set.sql")
    @DisplayName("Create scrollable result set")
    public void createScrollableResultSet(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT * FROM TABLE_C3");
        rs.next();
        Assertions.assertDoesNotThrow(rs::previous);
    }

    /**
     * A result set can also be sensitive or insensitive.
     * Sensitive result set are updated whenever the DBMS is updated and reflect
     * any changes made to the DBMS or the table while insensitive ones do not
     */
    @Test
    @JdbcSql("c3/cursor_scrollable_set/create_insensitive_result_set.sql")
    @DisplayName("Create insensitive scrollable result set")
    public void createInsensitiveResultSet(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT ID, NAME FROM TABLE_C3 ORDER BY ID");
        Statement stmtUpdate = connection.createStatement();
        stmtUpdate.executeUpdate("UPDATE TABLE_C3 SET TABLE_C3.NAME = 'UPDATED_NAME' WHERE TABLE_C3.NAME = 'NAME_1'");
        rs.next();
        Assertions.assertNotEquals("UPDATED_NAME", rs.getString("NAME"));
    }

    /**
     * A result set can also be sensitive or insensitive.
     * Sensitive result set are updated whenever the DBMS is updated and reflect
     * any changes made to the DBMS or the table while insensitive ones do not
     */
    @Test
    @JdbcSql("c3/cursor_scrollable_set/create_sensitive_result_set.sql")
    @DisplayName("Create sensitive scrollable result set")
    public void createSensitiveResultSet(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT NAME, ID FROM TABLE_C3 ORDER BY ID");
        Statement stmtUpdate = connection.createStatement();
        stmtUpdate.executeUpdate("UPDATE TABLE_C3 SET TABLE_C3.NAME = 'UPDATED_NAME' WHERE TABLE_C3.ID= 1");
        rs.next();
        rs.refreshRow();
        Assertions.assertEquals("UPDATED_NAME", rs.getString("NAME"));
    }

    /**
     * Once a result set has been defined as scrollable
     * It can scrolled back and forth through next() and
     * previous()
     */
    @Test
    @JdbcSql("c3/cursor_scrollable_set/moving_cursor.sql")
    @DisplayName("Moving the cursor forward and backward")
    public void movingTheCursor(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT NAME, ID FROM TABLE_C3 ORDER BY ID");
        rs.next();
        rs.next();
        Assertions.assertEquals(2, rs.getInt("ID"));
        rs.previous();
        Assertions.assertEquals(1, rs.getInt("ID"));
    }

    /**
     * Once a result set has been defined as scrollable
     * It can also be scrolled to a precise position through
     * the absolute() method or through relative offset with
     * the relative() method
     */
    @Test
    @JdbcSql("c3/cursor_scrollable_set/moving_cursor_designated_row.sql")
    @DisplayName("Moving the cursor to a designated row")
    public void movingTheCursorToADesignatedRow(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT NAME, ID FROM TABLE_C3 ORDER BY ID");
        rs.absolute(2);
        Assertions.assertEquals(2, rs.getInt("ID"));
        rs.relative(-1);
        Assertions.assertEquals(1, rs.getInt("ID"));
    }

    /**
     * We can also check the position of the row's result set
     * through the method row
     */
    @Test
    @JdbcSql("c3/cursor_scrollable_set/get_cursor_position.sql")
    @DisplayName("Get the cursor position")
    public void getCursorPosition(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT NAME, ID FROM TABLE_C3 ORDER BY ID");
        rs.absolute(2);
        Assertions.assertEquals(2, rs.getRow());
    }


}
