package robot.crawler.reactor;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MoonlightBoxTest {

    private static final String html = """
                        
            <html>
            <head>
                <title>上海休闲娱乐-大众点评网</title>
                    <meta name="Keywords" content="上海,大众点评网"/>
                    <meta name="Description" content="大众点评网为您找到上海市附近10000家休闲娱乐商户信息。点击查看更多关于上海市地区附近休闲娱乐商户电话、地址、价格、评价、排行榜等详情。"/>
                    <meta http-equiv="mobile-agent" content="format=html5; url=https://m.dianping.com/shanghai/ch30">
                    <link rel="alternate" media="only screen and (max-width: 640px)" href="https://m.dianping.com/shanghai/ch30">
                <meta charset="UTF-8"/>
                <meta name="lx:category" content="dianping_nova">
                <!-- 设置 DNS-Prefetch（可选） -->
                <!-- 图文混排css -->
                <!-- 星级相关（星级图片以及星级分数）css -->
            </head>
            <body>
                <!--页头部分-->
                <div class="header-container"><div id="top-nav" class="top-nav"> <div class="top-nav-container clearfix"> <div class="group J-city-select"> <!--城市选择--> <a target="_blank" class="city J-city"><span class="map-icon"></span><span class="J-current-city">上海</span><span class="J-city-change">[更换]</span></a> <div class="city-list J-city-list Hide"> <div class="group clearfix"> <h3 class="title">国内城市</h3> <div> <a href="//www.dianping.com/shanghai" class="city-item">上海</a> <a href="//www.dianping.com/beijing" class="city-item">北京</a> <a href="//www.dianping.com/guangzhou" class="city-item">广州</a> <a href="//www.dianping.com/shenzhen" class="city-item">深圳</a> <a href="//www.dianping.com/tianjin" class="city-item">天津</a> <a href="//www.dianping.com/hangzhou" class="city-item">杭州</a> <a href="//www.dianping.com/nanjing" class="city-item">南京</a> <a href="//www.dianping.com/suzhou" class="city-item">苏州</a> <a href="//www.dianping.com/chengdu" class="city-item">成都</a> <a href="//www.dianping.com/wuhan" class="city-item">武汉</a> <a href="//www.dianping.com/chongqing" class="city-item">重庆</a> <a href="//www.dianping.com/xian" class="city-item">西安</a> </div> </div> <div class="group clearfix"> <h3 class="title">国外城市</h3> <div> <a href="//www.dianping.com/tokyo" class="city-item">东京</a> <a href="//www.dianping.com/seoul" class="city-item">首尔</a> <a href="//www.dianping.com/bangkok" class="city-item">曼谷</a> <a href="//www.dianping.com/paris" class="city-item">巴黎</a> </div> </div> <a class="all" href="//www.dianping.com/citylist">更多城市 &gt;</a> </div> </div> <div class="group quick-menu "> <span class="login-container J-login-container"> <a rel="nofollow" class="item " href="//account.dianping.com/login" data-click-name="login">你好，请登录/注册</a> </span> <span class="seprate">|</span> <a rel="nofollow" href="https://www.dianping.com/member/myinfo" class="item J-my-center-trigger">个人中心<i class="icon i-arrow"></i></a> <span class="seprate">|</span> <a target="_blank" class="item J-shop-serve-trigger">商户服务<i class="icon i-arrow"></i></a> <span class="seprate">|</span> <a target="_blank" class="item J-help-trigger">帮助中心<i class="icon i-arrow"></i></a> </div> <div class="panel my-center J-my-center-target Hide"> </div> <div class="panel my-center J-shop-serve-target Hide"> <a rel="nofollow" target="_blank" href="https://e.dianping.com/" data-click-name="shop-center">商户中心</a> <a rel="nofollow" target="_blank" href="https://e.dianping.com/claimcpc/page/index?source=dp" data-click-name="shop-coop">商户合作</a> <a rel="nofollow" target="_blank" href="https://daili.meituan.com/?comeFrom=dpwebMenu" data-click-name="daili">招募餐饮代理</a> <a rel="nofollow" target="_blank" href="https://daili.meituan.com/dz-zhaoshang" data-click-name="apollo">招募非餐饮代理</a> <a rel="nofollow" target="_blank" href="//b.meituan.com/canyin/PC">餐饮商户中心</a> </div> <div class="panel my-center J-help-target Hide"> <a rel="nofollow" target="_blank" href="https://rules-center.meituan.com?from=dianpingPC" data-click-name="useragreement">平台规则</a> <a rel="nofollow" target="_blank" href="//kf.dianping.com" data-click-name="kf">联系客服</a> </div> </div> </div> <div id="logo-input" class="logo-input life-conf"> <div class="logo-input-container clearfix"> <a title="大众点评网" href="/" class="logo logo-life"></a> <div class="search-box"> <div class="search-bar "> <span class="search-container clearfix"> <i class="i-search"></i> <span> <input id="J-search-input" class="J-search-input" x-webkit-speech="" x-webkit-grammar="builtin:translate" data-s-pattern="https://www.dianping.com/search/keyword/{0}/{1}_" data-s-epattern="https://www.dianping.com/shanghai/{0}" data-s-cateid="0" data-s-cityid="1" type="text" placeholder="搜索商户名、地址、菜名、外卖等" autocomplete="off" /> </span> <span class="search-bnt-panel"> <a target="_blank" class="search-btn search-channel-bnt J-search-btn" id="J-channel-bnt" data-s-chanid="30">频道搜索</a> <a target="_blank" class="search-btn search-all-bnt J-search-btn platform-btn" id="J-all-btn" data-s-chanid="0">全站搜索</a> </span> </span> <p class="hot-search J-hot-search"> </p> </div> </div> </div> </div> <div id="cate-channel" class="cate-container channel-cate-container life-conf"> <div class="nav-header"> <div class="navbar"> <span class="cate-item all-cate J-all-cate">全部休闲娱乐分类 <i class="primary-more"></i> </span> </div> </div> <div class="gradient"></div> <div id="cate-index" class="cate-index"> <div class="navwrap"> <div id="nav" > <div class="cate-nav J-cate-nav Hidden"> <ul class="first-cate J-primary-menu"> <li class="first-item"> <div class="primary-container"> <span class="span-container"> <a target="_blank" class="index-title">足疗洗浴</a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g141" data-category="life.zuliao" data-click-title="second" data-click-name="g141"><span>足疗按摩</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g140" data-category="life.zuliao" data-click-title="second" data-click-name="g140"><span>洗浴/汗蒸</span></a> </span> </div> <div class="sec-cate Hide" data-category="cate.life.zuliao" > <div class="groups"> <div class="group"> <div class="sec-title"> <span class="channel-title" href="">足疗洗浴</span> </div> <div class="sec-items"> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g141" data-category="life.zuliao" data-click-name="g141">足疗按摩</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g140" data-category="life.zuliao" data-click-name="g140">洗浴/汗蒸</a> </div> </div> </div> </div> </li> <li class="first-item"> <div class="primary-container"> <span class="span-container"> <a target="_blank" class="index-title">玩乐</a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch15/g135" data-category="life.wanle" data-click-title="second" data-click-name="g135"><span>KTV</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g133" data-category="life.wanle" data-click-title="second" data-click-name="g133"><span>酒吧</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g2754" data-category="life.wanle" data-click-title="second" data-click-name="g2754"><span>密室逃脱</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g20040" data-category="life.wanle" data-click-title="second" data-click-name="g20040"><span>轰趴馆</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g20041" data-category="life.wanle" data-click-title="second" data-click-name="g20041"><span>私人影院</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g137" data-category="life.wanle" data-click-title="second" data-click-name="g137"><span>游乐游艺</span></a> </span> </div> <div class="sec-cate Hide" data-category="cate.life.wanle" > <div class="groups"> <div class="group"> <div class="sec-title"> <span class="channel-title" href="">玩乐High</span> </div> <div class="sec-items"> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch15/g135" data-category="life.wanle" data-click-name="g135">KTV</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g133" data-category="life.wanle" data-click-name="g133">酒吧</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g2754" data-category="life.wanle" data-click-name="g2754">密室逃脱</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g20040" data-category="life.wanle" data-click-name="g20040">轰趴馆</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g20041" data-category="life.wanle" data-click-name="g20041">私人影院</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g137" data-category="life.wanle" data-click-name="g137">游乐游艺</a> </div> </div> </div> </div> </li> <li class="first-item"> <div class="primary-container"> <span class="span-container"> <a target="_blank" class="index-title">休闲活动</a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g134" data-category="life.xiuxianhd" data-click-title="second" data-click-name="g134"><span>茶馆</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g20042" data-category="life.xiuxianhd" data-click-title="second" data-click-name="g20042"><span>网吧网咖</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g144" data-category="life.xiuxianhd" data-click-title="second" data-click-name="g144"><span>DIY手工坊</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g20038" data-category="life.xiuxianhd" data-click-title="second" data-click-name="g20038"><span>采摘/农家乐</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g142" data-category="life.xiuxianhd" data-click-title="second" data-click-name="g142"><span>文化艺术</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g6694" data-category="life.xiuxianhd" data-click-title="second" data-click-name="g6694"><span>桌游</span></a> </span> </div> <div class="sec-cate Hide" data-category="cate.life.xiuxianhd" > <div class="groups"> <div class="group"> <div class="sec-title"> <span class="channel-title" href="">休闲活动</span> </div> <div class="sec-items"> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g134" data-category="life.xiuxianhd" data-click-name="g134">茶馆</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g20042" data-category="life.xiuxianhd" data-click-name="g20042">网吧网咖</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g144" data-category="life.xiuxianhd" data-click-name="g144">DIY手工坊</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g20038" data-category="life.xiuxianhd" data-click-name="g20038">采摘/农家乐</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g142" data-category="life.xiuxianhd" data-click-name="g142">文化艺术</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g6694" data-category="life.xiuxianhd" data-click-name="g6694">桌游</a> </div> </div> </div> </div> </li> <li class="first-item"> <div class="primary-container"> <span class="span-container"> <a target="_blank" class="index-title">其他休闲娱乐</a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g33857" data-category="life.qitaxxyl" data-click-title="second" data-click-name="g33857"><span>VR</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g34089" data-category="life.qitaxxyl" data-click-title="second" data-click-name="g34089"><span>团建拓展</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g32732" data-category="life.qitaxxyl" data-click-title="second" data-click-name="g32732"><span>棋牌室</span></a> <a target="_blank" class="index-item" href="//www.dianping.com/shanghai/ch30/g156" data-category="life.qitaxxyl" data-click-title="second" data-click-name="g156"><span>桌球馆</span></a> <a target="_blank" class="index-item" href="http://www.dianping.com/shanghai/ch30/g156" data-category="life.qitaxxyl" data-click-title="second" data-click-name="g156"><span>更多</span></a> </span> </div> <div class="sec-cate Hide" data-category="cate.life.qitaxxyl" > <div class="groups"> <div class="group"> <div class="sec-title"> <span class="channel-title" href="">其他休闲娱乐</span> </div> <div class="sec-items"> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g33857" data-category="life.qitaxxyl" data-click-name="g33857">VR</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g20039" data-category="life.qitaxxyl" data-click-name="g20039">真人CS</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g32732" data-category="life.qitaxxyl" data-click-name="g32732">棋牌室</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g156" data-category="life.qitaxxyl" data-click-name="g156">桌球馆</a> <a target="_blank" class="second-item" href="http://www.dianping.com/shanghai/ch30/g156" data-category="life.qitaxxyl" data-click-name="g156">更多休闲娱乐</a> </div> </div> </div> </div> </li> </ul> </div> </div> </div> </div> </div></div>
                <div class="section Fix J-shop-search">
                        <div class="bread J_bread">
                                            <span>
                                                <a itemscope itemtype="http://data-vocabulary.org/Breadcrumb"  data-ga-index="1"
                                                     href="/shanghai/ch30" itemprop="url">
                                                    <span itemprop="title">
                                                        上海休闲娱乐
                                                    </span>
                                                </a>
                                            </span>
                        
                        
                        </div>
                        
                        
                        
                    <div class="navigation">
                    <!-- 频道 -->
            <!-- 频道 end -->
                        
            <!-- 分类 -->
            <div class="nav-category J_filter_category">
                <h4>分类:</h4>
                <a href="https://www.dianping.com/shanghai/ch30" class="def cur" data-cat-id="0" data-click-name="select_cate_all_click"><span>不限</span></a>
                <div class="nc-contain">
                    <div class="con">
                        <div id="classfy" class="nc-items">
                            <a href="https://www.dianping.com/shanghai/ch30/g141"  data-cat-id="141" data-click-name="select_cate_按摩/足疗_click"><span>按摩/足疗</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g135"  data-cat-id="135" data-click-name="select_cate_KTV_click"><span>KTV</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g140"  data-cat-id="140" data-click-name="select_cate_洗浴/汗蒸_click"><span>洗浴/汗蒸</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g133"  data-cat-id="133" data-click-name="select_cate_酒吧_click"><span>酒吧</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g2636"  data-cat-id="2636" data-click-name="select_cate_运动健身_click"><span>运动健身</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g134"  data-cat-id="134" data-click-name="select_cate_茶馆_click"><span>茶馆</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g2754"  data-cat-id="2754" data-click-name="select_cate_密室/沉浸互动剧_click"><span>密室/沉浸互动剧</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g34089"  data-cat-id="34089" data-click-name="select_cate_团建拓展_click"><span>团建拓展</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g20038"  data-cat-id="20038" data-click-name="select_cate_采摘/农家乐_click"><span>采摘/农家乐</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g50035"  data-cat-id="50035" data-click-name="select_cate_剧本杀_click"><span>剧本杀</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g137"  data-cat-id="137" data-click-name="select_cate_游戏厅_click"><span>游戏厅</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g144"  data-cat-id="144" data-click-name="select_cate_DIY手工坊_click"><span>DIY手工坊</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g20041"  data-cat-id="20041" data-click-name="select_cate_私人影院_click"><span>私人影院</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g20040"  data-cat-id="20040" data-click-name="select_cate_轰趴馆_click"><span>轰趴馆</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g20042"  data-cat-id="20042" data-click-name="select_cate_网吧/电竞_click"><span>网吧/电竞</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g6694"  data-cat-id="6694" data-click-name="select_cate_桌面游戏_click"><span>桌面游戏</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g32732"  data-cat-id="32732" data-click-name="select_cate_棋牌室_click"><span>棋牌室</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g142"  data-cat-id="142" data-click-name="select_cate_文化艺术_click"><span>文化艺术</span></a>
                            <a href="https://www.dianping.com/shanghai/ch30/g34090"  data-cat-id="34090" data-click-name="select_cate_新奇体验_click"><span>新奇体验</span></a>
                        </div>
                        
                    </div>
                </div>
            </div>
            <!-- 分类 end -->
                        
                        
                        
                        
                        
            <!-- 推荐 -->
                        
            <!-- 推荐 end -->
                        
                    <div class="nav-category nav-tabs J_filter_region">
                    <h4>地点:</h4>
                    <a href="https://www.dianping.com/shanghai/ch30" class="def cur"><span>不限</span></a>
                    <div class="nc-contain">
                        <div id='J_nav_tabs' class="tabs">
                            <a href="javascript:;" nav="#nav-tab|0|0" data-click-name="select_reg_hot_click" data-click-title="hot"><span class="tit">热门商区</span></a>
                            <a href="javascript:;" nav="#nav-tab|0|1" data-click-name="select_reg_biz_click" data-click-title="biz"><span class="tit">行政区</span></a>
                                <a href="javascript:;" nav="#nav-tab|0|2" data-click-name="select_reg_metro_click" data-click-title="metro"><span class="tit">地铁线</span></a>
                        </div>
                        <div id="J_nt_items" class="con">
                            <div id="bussi-nav" class="nc-items">
                                    <a href="https://www.dianping.com/shanghai/ch30/r812" data-cat-id="812" data-click-name="select_reg_hot_click" data-click-title="静安寺" ><span>静安寺</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r801" data-cat-id="801" data-click-name="select_reg_hot_click" data-click-title="陆家嘴" ><span>陆家嘴</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r811" data-cat-id="811" data-click-name="select_reg_hot_click" data-click-title="南京西路" ><span>南京西路</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r838" data-cat-id="838" data-click-name="select_reg_hot_click" data-click-title="打浦桥/田子坊" ><span>打浦桥/田子坊</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r854" data-cat-id="854" data-click-name="select_reg_hot_click" data-click-title="五角场/大学区" ><span>五角场/大学区</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r2528" data-cat-id="2528" data-click-name="select_reg_hot_click" data-click-title="龙柏地区" ><span>龙柏地区</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r836" data-cat-id="836" data-click-name="select_reg_hot_click" data-click-title="新天地/马当路" ><span>新天地/马当路</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r842" data-cat-id="842" data-click-name="select_reg_hot_click" data-click-title="中山公园/江苏路" ><span>中山公园/江苏路</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r865" data-cat-id="865" data-click-name="select_reg_hot_click" data-click-title="徐家汇" ><span>徐家汇</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r861" data-cat-id="861" data-click-name="select_reg_hot_click" data-click-title="南京东路" ><span>南京东路</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r872" data-cat-id="872" data-click-name="select_reg_hot_click" data-click-title="漕河泾/田林" ><span>漕河泾/田林</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r803" data-cat-id="803" data-click-name="select_reg_hot_click" data-click-title="世纪公园/科技馆" ><span>世纪公园/科技馆</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r840" data-cat-id="840" data-click-name="select_reg_hot_click" data-click-title="天山" ><span>天山</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r835" data-cat-id="835" data-click-name="select_reg_hot_click" data-click-title="淮海路" ><span>淮海路</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r860" data-cat-id="860" data-click-name="select_reg_hot_click" data-click-title="人民广场/南京路" ><span>人民广场/南京路</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r802" data-cat-id="802" data-click-name="select_reg_hot_click" data-click-title="八佰伴" ><span>八佰伴</span></a>
                            </div>
                            <div id="region-nav" class="nc-items">
                                        <a href="https://www.dianping.com/shanghai/ch30/r3" data-cat-id="3" data-click-name="select_reg_biz_click" data-click-title="静安区" ><span>静安区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r4" data-cat-id="4" data-click-name="select_reg_biz_click" data-click-title="长宁区" ><span>长宁区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r2" data-cat-id="2" data-click-name="select_reg_biz_click" data-click-title="徐汇区" ><span>徐汇区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r10" data-cat-id="10" data-click-name="select_reg_biz_click" data-click-title="杨浦区" ><span>杨浦区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r6" data-cat-id="6" data-click-name="select_reg_biz_click" data-click-title="黄浦区" ><span>黄浦区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r9" data-cat-id="9" data-click-name="select_reg_biz_click" data-click-title="虹口区" ><span>虹口区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r7" data-cat-id="7" data-click-name="select_reg_biz_click" data-click-title="普陀区" ><span>普陀区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r12" data-cat-id="12" data-click-name="select_reg_biz_click" data-click-title="闵行区" ><span>闵行区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r13" data-cat-id="13" data-click-name="select_reg_biz_click" data-click-title="宝山区" ><span>宝山区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r5" data-cat-id="5" data-click-name="select_reg_biz_click" data-click-title="浦东新区" ><span>浦东新区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r5937" data-cat-id="5937" data-click-name="select_reg_biz_click" data-click-title="松江区" ><span>松江区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r5938" data-cat-id="5938" data-click-name="select_reg_biz_click" data-click-title="嘉定区" ><span>嘉定区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r5939" data-cat-id="5939" data-click-name="select_reg_biz_click" data-click-title="青浦区" ><span>青浦区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r8847" data-cat-id="8847" data-click-name="select_reg_biz_click" data-click-title="金山区" ><span>金山区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/r8846" data-cat-id="8846" data-click-name="select_reg_biz_click" data-click-title="奉贤区" ><span>奉贤区</span></a>
                                        <a href="https://www.dianping.com/shanghai/ch30/c3580" data-cat-id="3580" data-click-name="select_reg_biz_click" data-click-title="崇明区" ><span>崇明区</span></a>
                            </div>
                            <div id="metro-nav" class="nc-items">
                                    <a href="https://www.dianping.com/shanghai/ch30/r1325" data-cat-id="1325" data-click-name="select_reg_metro_click" data-click-title="1号线" ><span>1号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r1326" data-cat-id="1326" data-click-name="select_reg_metro_click" data-click-title="2号线" ><span>2号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r1327" data-cat-id="1327" data-click-name="select_reg_metro_click" data-click-title="3号线" ><span>3号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r1328" data-cat-id="1328" data-click-name="select_reg_metro_click" data-click-title="4号线" ><span>4号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r90680" data-cat-id="90680" data-click-name="select_reg_metro_click" data-click-title="5号线支线" ><span>5号线支线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r1329" data-cat-id="1329" data-click-name="select_reg_metro_click" data-click-title="5号线" ><span>5号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r1330" data-cat-id="1330" data-click-name="select_reg_metro_click" data-click-title="6号线" ><span>6号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r3110" data-cat-id="3110" data-click-name="select_reg_metro_click" data-click-title="7号线" ><span>7号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r1331" data-cat-id="1331" data-click-name="select_reg_metro_click" data-click-title="8号线" ><span>8号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r1332" data-cat-id="1332" data-click-name="select_reg_metro_click" data-click-title="9号线" ><span>9号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r90243" data-cat-id="90243" data-click-name="select_reg_metro_click" data-click-title="10号线支线" ><span>10号线支线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r6338" data-cat-id="6338" data-click-name="select_reg_metro_click" data-click-title="10号线" ><span>10号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r6339" data-cat-id="6339" data-click-name="select_reg_metro_click" data-click-title="11号线" ><span>11号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r90244" data-cat-id="90244" data-click-name="select_reg_metro_click" data-click-title="11号线支线" ><span>11号线支线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r25986" data-cat-id="25986" data-click-name="select_reg_metro_click" data-click-title="12号线" ><span>12号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r8135" data-cat-id="8135" data-click-name="select_reg_metro_click" data-click-title="13号线" ><span>13号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r94812" data-cat-id="94812" data-click-name="select_reg_metro_click" data-click-title="14号线" ><span>14号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r93979" data-cat-id="93979" data-click-name="select_reg_metro_click" data-click-title="15号线" ><span>15号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r26247" data-cat-id="26247" data-click-name="select_reg_metro_click" data-click-title="16号线" ><span>16号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r83033" data-cat-id="83033" data-click-name="select_reg_metro_click" data-click-title="17号线" ><span>17号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r93971" data-cat-id="93971" data-click-name="select_reg_metro_click" data-click-title="18号线" ><span>18号线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r90029" data-cat-id="90029" data-click-name="select_reg_metro_click" data-click-title="磁浮线" ><span>磁浮线</span></a>
                                    <a href="https://www.dianping.com/shanghai/ch30/r88868" data-cat-id="88868" data-click-name="select_reg_metro_click" data-click-title="浦江线" ><span>浦江线</span></a>
                            </div>
                        
                        
                        </div>
                    </div>
                </div>
                <!-- 地点 end -->
            <!-- navigation end -->
                    </div>
                    <div class="content-wrap">
                        <div class="shop-wrap">
                            <div class="content">
                                    <div class="filter-box J_filter_box">
                <!-- classify -->
                <div class="filt-classify">
                    <a href="https://www.dianping.com/shanghai/ch30/m3" rel="nofollow" data-click-name="filter_booking_click" class=" "><i class="icon-check"></i>有团购<i class="icon-hot"></i></a>
                        
                    <a href="https://www.dianping.com/shanghai/ch30/m5" rel="nofollow" data-click-name="filter_price_click" class=" " ><i class="icon-check"></i>可订座</a>
                        
                </div>
                <!-- classify end -->
                <!-- service -->
                <div class="filt-service">
                    <ul>
                        <li><a href="https://www.dianping.com/shanghai/ch30" rel="nofollow" class="cur" data-click-name="sort_default_click">智能</a><em class="sep">|</em></li>
                        <li><a href="https://www.dianping.com/shanghai/ch30/o3" rel="nofollow"  data-click-name="sort_review_click">好评<i class="icon-arr-down"></i></a><em class="sep">|</em></li>
                        <li><a href="https://www.dianping.com/shanghai/ch30/o2" rel="nofollow"  data-click-name="sort_hot_click">人气<i class="icon-arr-down"></i></a><em class="sep">|</em></li>
                            <li><a href="https://www.dianping.com/shanghai/ch30/o4" rel="nofollow"  data-click-name="sort_custom_总分_click">总分<i class="icon-arr-down"></i></a><em class="sep">|</em></li>
                            <li class="fs-slt">
                                <a href="##"  >其他排序<i class="icon-arr-extend"></i></a>
                                <em class="sep">|</em>
                                <div class="slt-list">
                                    <span class="tit">其他排序<i class="icon-arr-packup"></i></span>
                                            <a href="https://www.dianping.com/shanghai/ch30/o11" rel="nofollow" data-order="2" data-click-name="sort_other_评价最多_click">评价最多</a>
                                </div>
                            </li>
                        
                        <li class="fs-slt">
                            <a  href="##" class="fs-price-tit " ><span class="avgprice"> 人均<i class="icon-arr-extend"></i></span></a>
                            <div class="slt-list per-capita">
                                <span class="tit">人均<i class="icon-arr-packup"></i></span>
                        
                                <a href="https://www.dianping.com/shanghai/ch30/o9" data-click-name="sort_avgprice_max_click" rel="nofollow" title="">人均最高</a>
                                <a href="https://www.dianping.com/shanghai/ch30/o8" data-click-name="sort_avgprice_min_click" rel="nofollow" title="">人均最低</a>
                        
                                <div class="ipt-price J_bar-range">
                                    <span class="i-box"><span class="icon">¥</span><input class="J_range-min" type="text" value=""></span>
                                    <span>-</span>
                                    <span class="i-box"><span class="icon">¥</span><input class="J_range-max" type="text" value=""></span>
                                    <div class="btn-box">
                                        <a href="javascript:void(0);" data-click-name="sort_avgprice_custom_click" title="" class="confirm J_range-btn" data-url="/shanghai/ch30/{0}">确定</a>
                                        <a href="javascript:void(0);" title="" class="reset J_range-reset">重置</a>
                                    </div>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
                <!-- service end -->
            </div>
                               \s
            <div class="shop-list J_shop-list shop-all-list" id="shop-all-list">
              <ul>
              <li class="" >
                <div class="pic" >
                  <a onclick="LXAnalytics('moduleClick', 'shoppic')" target="_blank" href="https://www.dianping.com/shop/k3dUDbu9sSxsslT6" data-click-name="shop_img_click" data-shopid="k3dUDbu9sSxsslT6" rel="nofollow" title=""  >
                    <img title="Lastdrop Whisky Bar·淳尽酒吧" alt="Lastdrop Whisky Bar·淳尽酒吧" data-src="https://p0.meituan.net/dpmerchantpic/581702f82811a919da6a1b87783debe13643853.jpg%40340w_255h_1e_1c_1l%7Cwatermark%3D0"
                       src="https://p0.meituan.net/dpmerchantpic/581702f82811a919da6a1b87783debe13643853.jpg%40340w_255h_1e_1c_1l%7Cwatermark%3D0"/>
                  </a>
                </div>
                        
                <div class="txt">
                  <div class="tit">
                    <a onclick="LXAnalytics('moduleClick', 'shopname');document.hippo.ext({cl_i:1,query_id:'f0f8700c-241b-445f-8edc-a1e1d2bd33cd'}).mv('cl_to_s','k3dUDbu9sSxsslT6');" data-click-name="shop_title_click" data-shopid="k3dUDbu9sSxsslT6" data-hippo-type="shop" title="Lastdrop Whisky Bar·淳尽酒吧" target="_blank" href="https://www.dianping.com/shop/k3dUDbu9sSxsslT6"  >
                        <h4>Lastdrop Whisky Bar·淳尽酒吧</h4>
                    </a>
                        
                        
                    <div class="promo-icon J_promo_icon">
                        
                          <a rel="nofollow" data-click-name="shop_group_icon_click" data-shopid="k3dUDbu9sSxsslT6"
                            target="_blank" href="http://t.dianping.com/deal/720271010" title="仅售398元，价值613元【好酒不见】整瓶金酒+汤力水"  class="igroup"\s
                            data-hippo-dealgrp_type="" data-hippo-dealgrp_id="720271010">
                          </a>
                        
                    </div>
                        
                        
                        
                  </div>
                        
                  <div class="comment">
                        
                    <div class="nebula_star">
                      <div class="star_icon">
                          <span class="star star_50 star_sml"></span>
                          <span class="star star_50 star_sml"></span>
                          <span class="star star_50 star_sml"></span>
                          <span class="star star_50 star_sml"></span>
                          <span class="star star_50 star_sml"></span>
                      </div>
                    </div>
                        
                      <a onclick="LXAnalytics('moduleClick', 'shopreview')" href="https://www.dianping.com/shop/k3dUDbu9sSxsslT6#comment" class="review-num" data-click-name="shop_iwant_review_click" data-shopid="k3dUDbu9sSxsslT6" target="_blank" module="list-readreview"\s
                          rel="nofollow">
                          <b>165</b>
            条评价</a>
                        
                    <em class="sep">|</em>
                    <a onclick="LXAnalytics('moduleClick', 'shopprice')" href="https://www.dianping.com/shop/k3dUDbu9sSxsslT6" class="mean-price" data-click-name="shop_avgprice_click" data-shopid="k3dUDbu9sSxsslT6" target="_blank" rel="nofollow" >
                        人均
                        <b>￥124</b>
                        </span>
                    </a>
                        
                  </div>
                        
                  <div class="tag-addr">
                    <a href = "https://www.dianping.com/shanghai/ch30/g50122" data-click-name="shop_tag_cate_click" data-shopid="k3dUDbu9sSxsslT6" ><span class="tag">综合清吧</span></a>
                    <em class="sep">|</em>
                    <a href = "https://www.dianping.com/shanghai/ch30/r808" data-click-name="shop_tag_region_click" data-shopid="k3dUDbu9sSxsslT6" ><span class="tag">张江</span></a>
                  </div>
                        
                        
                </div>
                        
                        
                        
                  <div class="svr-info">
                        
                        <div deal-type="DEAL_GROUP" class="si-deal d-packup">
                        <a href="javascript:void(0);" title="" class="more J_more" data-click-name="shop_groupdeal_more_click"  >更多5单团购<i class="icon-arr-extend"></i></a>
                        <a target="_blank" href="http://t.dianping.com/deal/720271010" data-click-name="shop_info_groupdeal_click"  title="团购：仅售398元，价值613元【好酒不见】整瓶金酒+汤力水"
                          \s
                           >
                            <span class="tit">团购：</span>仅售398元，价值613元【好酒不见】整瓶金酒+汤力水
                        </a>
                        <a target="_blank" href="http://t.dianping.com/deal/717906914" data-click-name="shop_info_groupdeal_click"  title="团购：仅售90元，价值100元代金券"
                          \s
                           >
                            <span class="tit">团购：</span>仅售90元，价值100元代金券
                        </a>
                        <a target="_blank" href="http://t.dianping.com/deal/677873640" data-click-name="shop_info_groupdeal_click"  title="团购：仅售368元，价值550元【朝酒晚舞】威士忌多人可乐桶3L"
                          \s
                           >
                            <span class="tit">团购：</span>仅售368元，价值550元【朝酒晚舞】威士忌多人可乐桶3L
                        </a>
                        <a target="_blank" href="http://t.dianping.com/deal/844774401" data-click-name="shop_info_groupdeal_click"  title="团购：仅售149元，价值174元【吃点喝点】披萨薯条起泡葡萄酒各一份"
                          \s
                           >
                            <span class="tit">团购：</span>仅售149元，价值174元【吃点喝点】披萨薯条起泡葡萄酒各一份
                        </a>
                        <a target="_blank" href="http://t.dianping.com/deal/644089490" data-click-name="shop_info_groupdeal_click"  title="团购：仅售59元，价值95元【大学特价】单杯酒小食套餐"
                          \s
                           >
                            <span class="tit">团购：</span>仅售59元，价值95元【大学特价】单杯酒小食套餐
                        </a>
                        <a target="_blank" href="http://t.dianping.com/deal/676545350" data-click-name="shop_info_groupdeal_click"  title="团购：仅售268元，价值368元【不误正夜】经典啤酒套餐八瓶+小食"
                          \s
                           >
                            <span class="tit">团购：</span>仅售268元，价值368元【不误正夜】经典啤酒套餐八瓶+小食
                        </a>
                        </div>
                  </div>
                        
                <div class="operate J_operate Hide">
                  <a href="javascript:void(0);" rel="nofollow"  title="" class="o-nearby J_o-nearby" data-click-name="shop_nearby_click" data-shopid="k3dUDbu9sSxsslT6" data-sname="Lastdrop Whisky Bar·淳尽酒吧" data-url="/search/around/1/0_k3dUDbu9sSxsslT6{keyword}">附近</a>
                </div>
                        
                </li>
              </ul>
            </div>
                            </div>
                               \s
                        
                        
                        
            	<div class="page">
                        
                        
            					<a class="cur">1</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p2" data-ga-page="2" class="PageLink" title="2">2</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p3" data-ga-page="3" class="PageLink" title="3">3</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p4" data-ga-page="4" class="PageLink" title="4">4</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p5" data-ga-page="5" class="PageLink" title="5">5</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p6" data-ga-page="6" class="PageLink" title="6">6</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p7" data-ga-page="7" class="PageLink" title="7">7</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p8" data-ga-page="8" class="PageLink" title="8">8</a>
            					<a href="https://www.dianping.com/shanghai/ch30/p9" data-ga-page="9" class="PageLink" title="9">9</a>
                        
            				<span class="PageMore">...</span>
            			<a href="https://www.dianping.com/shanghai/ch30/p50" data-ga-page="50" class="PageLink" title="50">50</a>
                        
            			<a href="https://www.dianping.com/shanghai/ch30/p2" data-ga-page="2" class="next" title="下一页">下一页</a>
            	</div>
                        
                        
                        
            <div class="sear-result no-result">
               <h4>商户没有被收录？</h4>
               <div class="other-way">
                <a href="https://www.dianping.com/addshop/1_?k=" class="" id="popMbox">添加商户</a>
               </div>
               <div class="evaluation J_evaluation">
                您对搜索结果：<a href="javascript:void(0);" rel="nofollow" class="y J_good choice"><i></i>满意</a><a href="javascript:void(0);" rel="nofollow"  class="n J_no choice"><i></i>不满</a>
                <div class="y-result y-first J_sucTip Hide"><i></i>非常感谢对大众点评的支持</div>
                <div class="y-result y-second Hide J_sucTip"><i></i>请勿重复提交</div>
                <div class="no-box msg-box J_user-advice Hide">
                 <h4>遇到什么问题？</h4>
                 <i class="close" data-click-name="nobox_quit_click"></i>
                 <div class="" data-click-name="nobox_textarea_click">
                  <textarea>请输入...</textarea>
                 </div>
                 <div class="btn">
                  <a href="javascript:void(0);" class="del" data-click-name="nobox_cancel_click">取消</a>
                  <a href="javascript:void(0);" class="save" data-click-name="nobox_confirm_click">提交</a>
                 </div>
                </div>
               </div>
              </div>
                        
                        
            <section class="foot-links">
                    <!--品牌馆新增底部内链-->
                    <!--全国大全-->
                    <!--同城推荐-->
                    <!--城市推荐菜-->
                    <!--城市美食-->
                    <!--生活导航-->
                        
                    <!--热门城市-->
                    <!--品牌馆新增底部内链-->
                    <!--全国大全-->
                    <!--同城推荐-->
                    <!--城市推荐菜-->
                    <!--城市美食-->
                    <!--生活导航-->
                        
                    <!--热门城市-->
                    <!--品牌馆新增底部内链-->
                    <!--全国大全-->
                    <!--同城推荐-->
                    <!--城市推荐菜-->
                    <!--城市美食-->
                        <dl class="linksItem J-city-allfood">
                            <dt class="b-left">
                            <p>上海休闲娱乐:</p>
                            </dt>
                            <dd class="b-right">
                                <ul class="b-ul char-content">
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g141" data-click-name="footer_nav_click">上海按摩/足疗</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g135" data-click-name="footer_nav_click">上海KTV</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g140" data-click-name="footer_nav_click">上海洗浴/汗蒸</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g133" data-click-name="footer_nav_click">上海酒吧</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g2636" data-click-name="footer_nav_click">上海运动健身</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g134" data-click-name="footer_nav_click">上海茶馆</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g2754" data-click-name="footer_nav_click">上海密室/沉浸互动剧</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g34089" data-click-name="footer_nav_click">上海团建拓展</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g20038" data-click-name="footer_nav_click">上海农家乐</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g50035" data-click-name="footer_nav_click">上海剧本杀</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g137" data-click-name="footer_nav_click">上海游戏厅</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g144" data-click-name="footer_nav_click">上海DIY手工坊</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g20041" data-click-name="footer_nav_click">上海私人影院</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g20040" data-click-name="footer_nav_click">上海轰趴馆</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g20042" data-click-name="footer_nav_click">上海网吧/电竞</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g6694" data-click-name="footer_nav_click">上海桌面游戏</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g32732" data-click-name="footer_nav_click">上海棋牌室</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g142" data-click-name="footer_nav_click">上海文化艺术</a>
                                        </li>
                                        <li>
                                            <a target="_blank" href="/shanghai/ch30/g34090" data-click-name="footer_nav_click">上海新奇体验</a>
                                        </li>
                                </ul>
                            </dd>
                            <p class="moreover J-moreover showhide" style="visibility: visible;">更多</p>
                        </dl>
                    <!--生活导航-->
                        
                    <!--热门城市-->
                    <!--品牌馆新增底部内链-->
                    <!--全国大全-->
                    <!--同城推荐-->
                    <!--城市推荐菜-->
                    <!--城市美食-->
                    <!--生活导航-->
                        
                    <!--热门城市-->
                    <!--品牌馆新增底部内链-->
                    <!--全国大全-->
                    <!--同城推荐-->
                    <!--城市推荐菜-->
                    <!--城市美食-->
                    <!--生活导航-->
                        
                    <!--热门城市-->
            </section>
                        
                        
            <div class="J_mkt-group-1"></div>
                        </div>
                       \s
                    <div class="aside">
                        
                <div class="J_mkt-group-2"></div>
                <div class="J_aside-qrcode"></div>
                        
                <div class="J_midas-3"></div>
                        
                    <div class="J_mkt-group-3"></div>
                </div>
                        
                    </div>
                        
                    <a href="#top" class="to-top J_to-top Hide"><i></i></a>
                </div>
                <div class="footer-container"><div id="channel-footer" class="channel-footer"> <p class="links"> <a target="_blank" href="https://about.meituan.com" rel="nofollow">关于我们</a>| <a target="_blank" href="https://dpapp-appeal.meituan.com/#/shopCreditRegulationPC" rel="nofollow">商户诚信公约</a>| <a target="_blank" href="https://rules-center.meituan.com/?from=dianpingPC" rel="nofollow">规则中心</a>| <a target="_blank" href="https://about.meituan.com/news/report" rel="nofollow">媒体报道</a>| <a target="_blank" href="https://e.dianping.com/claimcpc/page/index?source=dp" rel="nofollow">商户入驻</a>| <a target="_blank" href="//www.dianping.com/business/" rel="nofollow">推广服务</a>| <a target="_blank" href="https://join.dianping.com/" rel="nofollow">人才招聘</a>| <span class="links-container"> <a class="ext-links" href="javascript:void(0);" rel="nofollow">最新咨询</a>| </span> <a target="_blank" rel="nofollow" href="https://about.meituan.com/contact?source=dp" rel="nofollow">联系我们</a>| <a target="_blank" href="http://www.dianping.com/app/download">应用下载</a> </p> <div class="ext-container Hide"> <div class="link-items Hide"> <a target="_blank" href="//www.dianping.com/discovery/"><span>资讯评论精选</span></a> </div> </div> <p class="rights"> <span style="margin-right:10px;">©2003-2022 dianping.com, All Rights Reserved.</span> <span>本站发布的所有内容，未经许可，不得转载，详见 <a rel="nofollow" class="G" href="https://rules-center.meituan.com/rules-detail/69">《知识产权声明》</a>。 </span> </p> </div> <script> var _hmt = _hmt || []; (function() { var hm = document.createElement("script"); hm.src = "https://hm.baidu.com/hm.js?602b80cf8079ae6591966cc70a3940e7"; var s = document.getElementsByTagName("script")[0]; s.parentNode.insertBefore(hm, s); })(); </script> <script> (function(){var h=navigator.userAgent;var i=navigator.appName;var b=i.indexOf("Microsoft Internet Explorer")!==-1;if(!b){return false}var d=/MSIE (\\d+).0/g;var e=d.exec(h);if(e&&e.length&&e[1]<9){var j='<div class="browser-overlay"></div><div id="browser-ie-con" class="browser-ie-con"><div id="browser-close" class="close">×</div><div class="browser-download chrome"><a href="//www.google.cn/chrome/browser/desktop/index.html?utm_dp" target="_black" title="chrome"></a></div><div class="browser-download firefox"><a href="//www.firefox.com.cn/download/?utm_dp" target="_black" title="firefox"></a></div></div>';var f=document.createElement("div");f.id="browser-update-ie";f.className="browser-update-ie";f.innerHTML=j;document.body.appendChild(f);var a=document.documentElement.clientWidth||document.body.clientWidth;var c=document.getElementById("browser-ie-con").offsetWidth;var g=(a-c)/2;document.getElementById("browser-ie-con").style.left=g+"px";document.getElementById("browser-close").attachEvent("onclick",function(){document.getElementById("browser-update-ie").style.display="none"},false)}})(); </script></div>
            </body>
            </html>
            """;

    private static Document doc;

    @BeforeAll
    public static void setUp() {
        Parser p = Parser.htmlParser();
        doc = p.parseInput(html, "https://www.dianping.com");
    }

    @Test
    public void testIChildTagWithClass() {
        Elements elements = doc.select("body > div.section.Fix.J-shop-search > div.content-wrap > div.shop-wrap > div.page > a.next");
        Assertions.assertEquals(1, elements.size());
    }

    @Test
    public void testClassAndPseudoClass() {
        Elements elements = doc.select("#shop-all-list > ul > li:nth-child(1) > div.txt > div.tit > a > h4");
        Assertions.assertEquals("Lastdrop Whisky Bar·淳尽酒吧", elements.get(0).text());
    }

    @Test
    public void testAttribute() {
        Elements elements = doc.select("#shop-all-list > ul > li:nth-child(1) > div.svr-info > div > a[data-click-name='shop_info_groupdeal_click']");
        Assertions.assertEquals(6, elements.size());
    }

    @Test
    public void testFindInElement() {
        Elements elements = doc.select("#shop-all-list > ul > li");
        Assertions.assertEquals(1, elements.size());
        Elements eles = elements.get(0).select("div.txt > div.tit > a");
        Assertions.assertEquals(1, eles.size());

    }
}
