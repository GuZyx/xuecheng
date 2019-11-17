<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
<br/>
遍历数据模型中list的学生信息(数据模型名称为stus)：
<table border="1px">
    <tr>
        <td>序号</td>
        <td>学生名称</td>
        <td>学生年龄</td>
        <td>学生金额</td>
        <td>出生日期</td>
    </tr>
    if指令
    <#if stus??>    <#-- 判断是否为空 -->
        <#list stus as stu >
        <tr
            <#if stu.name=="小明">style="color: red" </#if>
            <#if (stu.money<1000) >style="color: blue" </#if>>
            <td>${stu_index+1}</td>
            <td>${stu.name}</td>
        <#--gt相当于逻辑运算符的大于号，因为freemarker无法解析“ > ” 方法一 -->
        <#-- <#if (stu.age > 19) >方法二，用括号括起来 -->
            <td <#if stu.age gt 19 >style="background: black" </#if>>${stu.age}</td>
            <td>${stu.money}</td>
            <#--<td>${(stu.birthday?date)!''}</td>只显示年月日-->
            <#--<td>${(stu.birthday?time)!''}</td>只显示时间-->
            <#--<td>${(stu.birthday?datetime)!''}</td>显示年月日和时间-->
            <td>${(stu.birthday?string("yyyy年MM月dd日 hh-mm-ss"))!''}</td>自定义
        </tr>
        </#list>
    </#if>
</table>
学生的个数：${stus?size}<br/> <#-- 判断list的大小 -->
<br/>
遍历数据模型中的stuMap的stu1(map数据),第一种方法，在中括号当中填写map的key：
<br/>
姓名：${(stuMap['stu1'].name)!''}<br/> <#--缺省值判断，如果为空显示!后面的-->
年龄：${(stuMap['stu1'].age)!''}<br/>
金额：${(stuMap['stu1'].money)!''}<br/>
<br/>
取出数据模型中的stuMap的stu2(map数据),第二种方法，直接用 点 key：
<br/>
姓名：${stuMap.stu2.name}<br/>
年龄：${stuMap.stu2.age}<br/>
金额：${stuMap.stu2.money}<br/>
<br/>
遍历数据模型中的map的key，stuMap?keys就是key列表(是一个list)<br/>
<table border="1px">
    <tr>
        <td>序号</td>
        <td>学生名称</td>
        <td>学生年龄</td>
        <td>学生金额</td>
        <td>出生日期</td>
    </tr>
    <#list stuMap?keys as k>
        <tr>
            <td>${k_index+1}</td>
            <td>${stuMap[k].name}</td>
            <td>${stuMap[k].age}</td>
            <td>${stuMap[k].money}</td>
            <td>${(stuMap[k].birthday?string("yyyy年MM月dd日 hh-mm-ss"))!''}</td>
        </tr>
    </#list>
</table><br/><br/>

取出数据模型point变量：${point}  <br/>
将数字转为字符串：${point?c}<br/>
<br/>

将json字符串转化为对象：<br/>
<#assign text="{'bank':'工商银行','account':'10101920201920212'}"/>
<#assign data=text?eval />
开户行:${data.bank} 账号: ${data.account}
</body>
</html>