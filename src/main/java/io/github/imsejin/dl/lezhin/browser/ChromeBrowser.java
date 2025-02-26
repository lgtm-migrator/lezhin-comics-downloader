/*
 * Copyright 2020 Sejin Im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.imsejin.dl.lezhin.browser;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;
import io.github.imsejin.common.constant.OS;
import io.github.imsejin.dl.lezhin.util.PathUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @since 2.0.0
 */
@ThreadSafe
public final class ChromeBrowser {

    private static ChromeOptions options = new ChromeOptions().addArguments(ChromeOption.getArguments());

    private static boolean initialized;

    static {
        // Assigns chrome driver pathname.
        String fileName;
        if (OS.WINDOWS.isCurrentOS()) {
            fileName = "chromedriver.exe"; // for Microsoft Windows
        } else {
            fileName = "chromedriver"; // for Linux and macOS
        }

        Path currentPath = PathUtils.getCurrentPath();
        Path chromeDriverPath = currentPath.resolve(fileName);

        // Sets up pathname of web driver.
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, chromeDriverPath.toString());
    }

    @ExcludeFromGeneratedJacocoReport
    private ChromeBrowser() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    public static boolean isRunning() {
        return ChromeBrowser.initialized;
    }

    /**
     * @since 2.6.2
     */
    public static void debugging() {
        List<String> arguments = ChromeOption.getArguments();
        Stream.of(ChromeOption.HEADLESS, ChromeOption.NO_SANDBOX, ChromeOption.DISABLE_GPU)
                .map(ChromeOption::getArgument).forEach(arguments::remove);

        options = new ChromeOptions().addArguments(arguments);
    }

    public static ChromeDriver getDriver() {
        return SingletonLazyHolder.DRIVER;
    }

    public static void quitIfInitialized() {
        if (ChromeBrowser.initialized) {
            SingletonLazyHolder.DRIVER.quit();
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static class SingletonLazyHolder {
        static {
            ChromeBrowser.initialized = true;
        }

        private static final ChromeDriver DRIVER = new ChromeDriver(options);
    }

    // -------------------------------------------------------------------------------------------------

    /**
     * @since 2.6.2
     */
    @Getter
    @RequiredArgsConstructor
    private enum ChromeOption {
        /**
         * Opens browser in maximized mode.
         */
        START_MAXIMIZED("--start-maximized"),

        /**
         * Opens browser on private mode.
         */
        INCOGNITO("--incognito"),

        /**
         * Runs browser using CLI.
         */
        HEADLESS("--headless"),

        /**
         * Bypasses OS security model.
         *
         * @since 2.8.2
         */
        NO_SANDBOX("--no-sandbox"),

        /**
         * Disables GPU computation (applicable to Windows OS only).
         *
         * @since 2.8.2
         */
        DISABLE_GPU("--disable-gpu"),

        /**
         * Ignores certificate errors.
         */
        IGNORE_CERTIFICATE_ERRORS("--ignore-certificate-errors"),

        /**
         * Disables to check if Google Chrome is default browser on your device.
         *
         * @since 2.8.2
         */
        NO_DEFAULT_BROWSER_CHECK("--no-default-browser-check"),

        /**
         * Disables popup blocking.
         */
        DISABLE_POPUP_BLOCKING("--disable-popup-blocking"),

        /**
         * Disables installed extensions(plugins) of Google Chrome.
         *
         * @since 2.8.2
         */
        DISABLE_EXTENSIONS("--disable-extensions"),

        /**
         * Disables default web apps on Google Chrome’s new tab page
         * <p>
         * Chrome Web Store, Google Drive, Gmail, YouTube, Google Search, etc.
         */
        DISABLE_DEFAULT_APPS("--disable-default-apps"),

        /**
         * Disables Google translate feature.
         *
         * @since 2.8.2
         */
        DISABLE_TRANSLATE("--disable-translate"),

        /**
         * Disables detection for client side phishing.
         *
         * @since 2.8.2
         */
        DISABLE_CLIENT_SIDE_PHISHING_DETECTION("--disable-client-side-phishing-detection"),

        /**
         * Overcomes limited resource problems.
         *
         * @since 2.8.2
         */
        DISABLE_DEV_SHM_USAGE("--disable-dev-shm-usage");

        private final String argument;

        public static List<String> getArguments() {
            return Arrays.stream(values()).map(it -> it.argument).collect(toList());
        }
    }

}
