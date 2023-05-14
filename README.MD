# Web Crawler

## well-known web crawler

   1. [Apache Nutch](https://nutch.apache.org/) 

      部署Apache Nutch可以构建出自己的搜索引擎，该框架使用Tika等解析器解析抓取到的HTML， 使用Hadoop来存储数据，支持Solr或Elastic Search来检索。

   2. [wayback](https://archive.org/)

         openwayback使用htmlparser来发起HTTP请求，并解析DOM。htmlparser不支持css选择器，且已经非常久没有更新。

      python语言有著名的scrapy！
   
## design

   1. document only

      jsoup支持css选择器和xpath，可以方便我们从浏览器复制出元素选择器，进行获取。
      DOM解析比较耗内存！

   2. webdriver

      有些网站SEO做的很差，或者使用浏览器渲染，或者延迟加载等手段，很容易造成爬取信息与人访问不一致。
      此时借助Selenium WebDriver是个不错的选择（爬取速度确实会非常慢）。

## How to use

   1. check your browser and download webdriver

      Pick up webdriver version best match browser version, unzip it and make it executable.
      
      For Chrome, visit [chrome webdriver](https://chromedriver.chromium.org/)

      For Edge, visit [msedge webdriver](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/)

   2. write your task definition json or pick one from project test resources

   3. add program argument and vm options before you run

      For specify webdriver location, add vm option.

        For chrome: -Dwebdriver.chrome.driver; 

        For msedge: -Dwebdriver.edge.driver.

      For force use JDK httpclient, add vm option: -Dwebdriver.http.factory=jdk-http-client

      For specify running node webdriver option, add vm option -Dcrawler.application.json or add os environment variable CRAWLER_APPLICATION_JSON.
   
      For read input task definition, add argument: -r or --read, file:// or http:// or https:// are supported.

      For submit result, use -w or --write, file:// or http:// or https:// are supported.

   4. troubleshooting

      + Invalid Status code=403 text=Forbidden

        Chrome Driver started successfully but WebSocket listener error as chrome version vary:

        ```shell
        -Dcrawler.application.json="{\"arguments\":[\"--remote-allow-origins=*\"]}"
        ```

      + Unknown HttpClient factory jdk-http-client

        As maven-assembly-plugin package all classes into one fat jar, SPI implementation files under 'META-INF/services' directory conflict.

        Assume you exported M2_REPO, usually it is ${user.home}/.m2/repository, and try take selenium-http-jdk-client as classpath option like below:

        ```shell
        java -Dwebdriver.http.factory=jdk-http-client -cp $M2_REPO/org/seleniumhq/selenium/selenium-http-jdk-client/4.6.0/selenium-http-jdk-client-4.6.0.jar -jar crawler*-jar-with-dependencies.jar -r file://local.json -w file://result.json
        ```

      + Could not start a new session. Response code 500. Message: unknown error: Chrome failed to start: crashed

        try chrome option '--no-sandbox' as below:

        ```shell
        -Dcrawler.application.json="{\"arguments\":[\"--no-sandbox\"]}"
        ```
        
      + Could not start a new session. Response code 500. Message: unknown error: Chrome failed to start: Chrome failed to start: exit abnormally (unknown error: DevToolsActivePort file doesn't exist)
   
        try combine '--no-sandbox' and '--headless=new' as below:

        ```shell
        -Dcrawler.application.json="{\"arguments\":[\"--headless=new\", \"--no-sandbox\"]}"
        ```

## 免责声明

   本项目代码仅用于个人学习自动化使用，请勿用于其他用途。
   任何复制、修改、分发及运行由相应人员承担，与作者无关。
   
   任何人和机构针对本项目的运行、分发、修改，则视为同意上述免责声明。

      