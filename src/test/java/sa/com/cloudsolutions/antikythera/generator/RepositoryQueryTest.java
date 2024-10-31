package sa.com.cloudsolutions.antikythera.generator;

import com.github.javaparser.ast.body.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RepositoryQueryTest {

    private RepositoryQuery repositoryQuery;

    @BeforeEach
    void setUp() {
        repositoryQuery = new RepositoryQuery();
        repositoryQuery.setQuery("SELECT * FROM users WHERE id = :id");
        repositoryQuery.setIsNative(true);
    }

}
