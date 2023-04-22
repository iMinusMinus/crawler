module crawler {
    requires org.slf4j;

    requires jcommander;

    requires com.fasterxml.jackson.databind;

    requires janino;

    requires java.net.http;

    // webdriver
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.remote_driver;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.chromium_driver;
    requires org.seleniumhq.selenium.edge_driver;
    requires org.seleniumhq.selenium.support;

    //jsoup
    requires org.jsoup;

    // exports to selenium and janino(janino was unnamed module, so exports to ALL_UNNAMED )
    exports robot.crawler.reactor;

    opens robot.crawler to jcommander;
    opens robot.crawler.spec to com.fasterxml.jackson.databind;
}