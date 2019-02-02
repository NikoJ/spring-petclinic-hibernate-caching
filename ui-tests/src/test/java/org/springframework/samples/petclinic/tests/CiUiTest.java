package org.springframework.samples.petclinic.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;

import org.junit.Rule;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.samples.petclinic.util.FailingUiTestsWatcher;
import org.springframework.samples.petclinic.util.JmxUtil;
import org.springframework.samples.petclinic.util.TestContainerUtil;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public abstract class CiUiTest extends TestDataSource {
  private static DockerComposeContainer dockerComposeContainer = new DockerComposeContainer(new File("../docker-compose.yml"))
      .withLocalCompose(true)
      .withPull(false)
      .withExposedService("application_1", 8080, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(3)))
      .withExposedService("postgres_1", 5432);

  private static BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
      .withCapabilities(new ChromeOptions())
      .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null);

  static {
    dockerComposeContainer.start();
    TestContainerUtil.linkContainersNetworks(dockerComposeContainer, chrome, "application_1");
    chrome.start();
    Configuration.baseUrl = "http://application:8080";
    WebDriverRunner.setWebDriver(chrome.getWebDriver());
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      JmxUtil.generateJacocoDump();
      dockerComposeContainer.stop();
    }));
  }

  @Rule
  public FailingUiTestsWatcher failingUiTestsWatcher = new FailingUiTestsWatcher(chrome);

  @Override
  protected String jdbcHost() {
    return dockerComposeContainer.getServiceHost("postgres_1", 5432);
  }

  @Override
  protected int jdbcPort() {
    return dockerComposeContainer.getServicePort("postgres_1", 5432);
  }

  protected String homePath() {
    return "http://application:8080";
  }

  protected String apiLoginPath() {
    return "http://localhost:8070/login";
  }
}
