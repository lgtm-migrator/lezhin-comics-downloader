package io.github.imsejin.lzcodl.core;

import io.github.imsejin.common.util.JsonUtils;
import io.github.imsejin.lzcodl.TestUtils;
import io.github.imsejin.lzcodl.common.constant.URIs;
import io.github.imsejin.lzcodl.model.Product;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@DisplayName("ChromeBrowser")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChromeBrowserTest {

    private static ChromeDriver driver;

    @BeforeAll
    static void beforeAll() {
        String path = TestUtils.getDriverPath().getPath();
        System.setProperty("webdriver.chrome.driver", path);

        new File(path).setExecutable(true);
        driver = new ChromeDriver(new ChromeOptions().addArguments(ChromeBrowser.ChromeOption.getArguments()));
    }

    @AfterAll
    static void afterAll() {
        driver.quit();
    }

    @Test
    @SneakyThrows
    @DisplayName("method 'getVersion'")
    void getVersion() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("application.properties");

        @Cleanup
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        Properties properties = reader.lines().filter(it -> !it.matches("^\\s#.*"))
                .map(it -> it.split("=", 2))
                .collect(Properties::new, (props, arr) -> props.put(arr[0].trim(), arr[1].trim()), (props, arr) -> {
                });

        // Enables to build on development environment.
        if (properties.getProperty("environment").equals("local")) return;

        assertThat(ChromeBrowser.getVersion()).isNotNull().isEqualTo("92.0.4515.159");
    }

    @Test
    @DisplayName("Gets json using chrome driver")
    void getJson() {
        // when
        driver.get(URIs.COMIC.get("ko", "snail").toString());
        driver.executeScript("localStorage.setItem('product', JSON.stringify(window.__LZ_PRODUCT__.product));");
        String json = driver.getLocalStorage().getItem("product");

        // then
        assertThat(json)
                .as("The item 'product' of local storage must be json string")
                .matches("\\{\"\\w.*\":.+}");
        Product product = JsonUtils.toObject(json, Product.class);
        assertThat(product)
                .as("The json string can be parsed to Product")
                .isExactlyInstanceOf(Product.class)
                .returns("아가씨와 우렁총각", it -> it.getDisplay().getTitle());
    }

    @Test
    @Disabled
    void findLoadedClass() throws Exception {
        // given
        Method findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        findLoadedClass.setAccessible(true);
        String className = ChromeBrowser.class.getName() + "$SingletonLazyHolder";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // when: 1
        Class<?> clazz = (Class<?>) findLoadedClass.invoke(classLoader, className);
        // then: 1
        assertThat(clazz).isNull();

        // when: 2
        try {
            ChromeBrowser.getDriver().quit();
        } catch (Throwable ignored) {
        }
        Class<?> loadedClass = (Class<?>) findLoadedClass.invoke(classLoader, className);
        // then: 2
        assertThat(loadedClass)
                .isNotNull()
                .isEqualTo(Class.forName(className));
    }

}
