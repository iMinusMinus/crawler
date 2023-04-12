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

   2. webdriver

      有些网站SEO做的很差，或者使用浏览器渲染，或者延迟加载等手段，很容易造成爬取信息与人访问不一致。
      此时借助Selenium WebDriver是个不错的选择（爬取速度确实会非常慢）。

      