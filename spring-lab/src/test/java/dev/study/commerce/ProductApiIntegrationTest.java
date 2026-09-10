package dev.study.commerce;

import dev.study.commerce.catalog.domain.Product;
import dev.study.commerce.catalog.domain.ProductStatus;
import dev.study.commerce.catalog.infrastructure.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ProductApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;

    @BeforeEach
    void reset() { products.deleteAll(); }

    private String body(String sku, String status) {
        return """
                {"sku":"%s","name":"수분 크림","price":18000,"status":"%s"}
                """.formatted(sku, status);
    }

    @Test
    void createCommitsAndPublicListReturnsOnlyActiveProducts() throws Exception {
        mvc.perform(post("/api/v1/admin/products").with(httpBasic("test-admin", "test-password"))
                        .with(csrf()).contentType(APPLICATION_JSON).content(body("CREAM", "ACTIVE")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.version").value(0));
        products.saveAndFlush(new Product("DRAFT", "초안", 10, ProductStatus.DRAFT));
        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].sku").value("CREAM"));
        assertThat(products.count()).isEqualTo(2);
    }

    @Test
    void duplicateSkuReturnsConflictAndDoesNotCommitAnExtraRow() throws Exception {
        products.saveAndFlush(new Product("CREAM", "원본", 10, ProductStatus.ACTIVE));
        mvc.perform(post("/api/v1/admin/products").with(httpBasic("test-admin", "test-password"))
                        .with(csrf()).contentType(APPLICATION_JSON).content(body("CREAM", "ACTIVE")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DATA_CONFLICT"));
        assertThat(products.count()).isEqualTo(1);
    }

    @Test
    void draftCannotBeFetchedByItsKnownId() throws Exception {
        var product = products.saveAndFlush(new Product("DRAFT", "초안", 10, ProductStatus.DRAFT));
        mvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void invalidBodyIsRejectedBeforePersistence() throws Exception {
        mvc.perform(post("/api/v1/admin/products").with(httpBasic("test-admin", "test-password"))
                        .with(csrf()).contentType(APPLICATION_JSON)
                        .content("""
                                {"sku":"lowercase","name":" ","price":-1,"status":"ACTIVE"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.violations").isArray());
        assertThat(products.count()).isZero();
    }

    @Test
    void invalidJsonAndIdentifiersUseClientErrorResponses() throws Exception {
        mvc.perform(get("/api/v1/products/not-a-uuid"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(post("/api/v1/admin/products").with(httpBasic("test-admin", "test-password"))
                        .with(csrf()).contentType(APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidPageBoundsAreRejected() throws Exception {
        for (String query : new String[]{"?page=-1", "?size=0", "?size=51", "?page=abc"}) {
            mvc.perform(get("/api/v1/products" + query))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
    }

    @Test
    void writesRequireAuthenticationAndAdminRole() throws Exception {
        mvc.perform(post("/api/v1/admin/products").with(csrf())
                        .contentType(APPLICATION_JSON).content(body("CREAM", "ACTIVE")))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/products").with(user("customer").roles("CUSTOMER"))
                        .with(csrf()).contentType(APPLICATION_JSON).content(body("CREAM", "ACTIVE")))
                .andExpect(status().isForbidden());
        assertThat(products.count()).isZero();
    }

    @Test
    void adminWritesStillRequireCsrfProtection() throws Exception {
        mvc.perform(post("/api/v1/admin/products").with(httpBasic("test-admin", "test-password"))
                        .contentType(APPLICATION_JSON).content(body("CREAM", "ACTIVE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthIsPublicButMetricsRequireAdmin() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics").with(httpBasic("test-admin", "test-password")))
                .andExpect(status().isOk());
    }
}
