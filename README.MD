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
      
      __For Chrome, visit chrome webdriver__

      __For Edge, visit msedge webdriver__

   2. write your task definition json or pick one from project test resources

   3. add program argument and vm options before you run

      __For read input task definition, use -r file://localFilePath or -r http://remoteHost:port/path__
      __For submit result, use -w file://localResultPath or -r https://remoteHost:port/endpoint__

      __For specify webdriver location, add vm option. For chrome: webdriver.chrome.driver; For msedge: webdriver.edge.driver__
      __For force use JDK httpclient, add vm option: webdriver.http.factory=jdk-http-client __

## 免责声明

   本项目代码仅用于个人学习自动化使用，请勿用于其他用途。
   任何复制、修改、分发及运行由相应人员承担，与作者无关。
   
   任何人和机构针对本项目的运行、分发、修改，则视为同意上述免责声明。

      