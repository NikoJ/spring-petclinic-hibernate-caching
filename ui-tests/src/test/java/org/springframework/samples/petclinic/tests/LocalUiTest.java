package org.springframework.samples.petclinic.tests;

import com.codeborne.selenide.Configuration;

import org.junit.BeforeClass;

public abstract class LocalUiTest extends TestDataSource {
  @BeforeClass
  public static void setUpClass() {
    Configuration.holdBrowserOpen = true;
    Configuration.baseUrl = "http://localhost:3000";
  }

  @Override
  protected String jdbcHost() {
    return "127.0.0.1";
  }

  @Override
  protected int jdbcPort() {
    return 5432;
  }
}
