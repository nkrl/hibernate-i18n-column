package nkrl.hibernate.i18n;

import jakarta.persistence.*;
import nkrl.hibernate.i18n.entity.Post;
import org.hibernate.Session;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;

public class HibernateEntityIntegrationTest {

    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    @BeforeAll
    public static void setUpPersistence() throws IOException {
        LogManager.getLogManager().readConfiguration(HibernateEntityIntegrationTest.class.getResourceAsStream("/logging.properties"));
        entityManagerFactory = Persistence.createEntityManagerFactory("nkrl.hibernate.i18n.entity");
    }

    @BeforeEach
    public void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
    }

    @Test
    public void entityCanBePersisted() {

        Post newPost = new Post();
        newPost.setTitle(Locale.GERMANY, "Nein");
        newPost.setTitle(Locale.ENGLISH, "No");
        entityManager.persist(newPost);
        entityManager.flush();

        TypedQuery<Post> query = entityManager.createQuery("from Post", Post.class);
        for (Post p : query.getResultList()) {
            assertThat(p.getTitle(Locale.GERMANY)).isEqualTo("Nein");
            assertThat(p.getTitle(Locale.ENGLISH)).isEqualTo("No");
        }

        /*Query<Map<String, Object>> columnQuery = (Query<Map<String, Object>>) entityManager.createQuery("from Post")
                .unwrap(Query.class)
                .setTupleTransformer(AliasToEntityMapResultTransformer.INSTANCE);
        Map<String, Object> columns = columnQuery.getSingleResult();

        assertThat(columns).containsKey("titles");*/
        Set<String> columns = new HashSet<>();
        entityManager.unwrap(Session.class).doWork(connection -> {
            Statement statement = connection.createStatement();
            // We are retrieving the table name from the @Table annotation because Hibernate offers no easy way to get it
            Table table = Post.class.getAnnotation(Table.class);
            String tableName = table.name();
            ResultSetMetaData meta = statement.executeQuery("select * from " + tableName).getMetaData();
            for (int column = 1; column <= meta.getColumnCount(); column++) {
                columns.add(meta.getColumnLabel(column));
            }
        });
        assertThat(columns).containsAnyOf("titles", "TITLES").doesNotContainAnyElementsOf(List.of("contents", "CONTENTS"));
    }

    @AfterEach
    public void finishTransaction() {
        entityManager.getTransaction().commit();
    }

    @AfterAll
    public static void cleanUpPersistence() {
        entityManagerFactory.close();
    }
}
