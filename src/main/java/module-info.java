module crawler {
    requires org.slf4j;

    requires jcommander;

    requires com.fasterxml.jackson.databind;

    // webdriver
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.remote_driver;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.chromium_driver;
    requires org.seleniumhq.selenium.edge_driver;
    requires org.seleniumhq.selenium.support;

    //jsoup
    requires org.jsoup;

    exports robot.crawler.reactor to org.seleniumhq.selenium.support;

    opens robot.crawler to jcommander;
    opens robot.crawler.spec to com.fasterxml.jackson.databind;
}