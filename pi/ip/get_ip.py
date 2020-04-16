import requests
from bs4 import BeautifulSoup
import smtplib
from email.mime.text import MIMEText
from email.utils import parseaddr, formataddr
from email.header import Header


def send(ip):
    try:
        from_addr = '18758298536@163.com'
        to_addr = 'blanexie@qq.com'
        msg = MIMEText(ip, 'plain', 'utf-8')
        msg['From'] = formataddr(['Python爱好者', '18758298536@163.com'])
        msg['To'] = formataddr(['管理员 ', 'blanexie@qq.com'])
        msg['Subject'] = Header('来自SMTP的问候……', 'utf-8').encode()

        server = smtplib.SMTP_SSL('smtp.163.comm', 465)
        server.set_debuglevel(1)
        server.login(from_addr, '7696634xzc')
        server.sendmail(from_addr, [to_addr], msg.as_string())
        server.quit()

    #
    # # 第一个参数：邮件的内容；第二个参数：邮件内容的格式，普通的文本，可以使用:plain,如果想使内容美观，可以使用:html；第三个参数：设置内容的编码，这里设置为:utf-8
    #         content = MIMEText(mail_content, 'plain', 'utf-8')
    #         reveivers = "765150816@qq.com"
    #         content['To'] = reveivers  # 设置邮件的接收者，多个接收者之间用逗号隔开
    #         content['From'] = str("18758298536@163.com")  # 邮件的发送者,最好写成str("这里填发送者")，不然可能会出现乱码
    #         content['Subject'] = "这是一封测试邮件"  # 邮件的主题
    #
    #         ##############使用qq邮箱的时候，记得要去开启你的qq邮箱的smtp服务；##############
    #         # 方法：
    #         # 1）登录到你的qq邮箱；
    #         # 2）找到首页顶部的【设置】并点击；
    #         # 3）找到【账户】这个选项卡并点击，然后在页面中找到“SMTP”相关字样，找到【开启】的超链接，点击后会告诉你开启方法（需要发个短信），然后按照指示操作，最终会给你一个密码，这个密码可以用于在代码中当作邮箱密码
    #         # 注意!!!:163邮箱之类的不知道要不要这些操作，如果是163邮箱你可以忽略此步骤
    #         ###########################################################################
    #         # 第一个参数：smtp服务地址（你发送邮件所使用的邮箱的smtp地址，在网上可以查到，比如qq邮箱为smtp.qq.com） 第二个参数：对应smtp服务地址的端口号
    #         smtp_server = smtplib.SMTP_SSL("smtp.163.comm", 25)
    #         # 第一个参数：发送者的邮箱账号 第二个参数：对应邮箱账号的密码
    #         smtp_server.login("18758298536@163.com", "7696634xzc")
    #         #################################
    #
    #         smtp_server.sendmail("18758298536@163.com", ["765150816@qq.com"], content.as_string())
    #         # 第一个参数：发送者的邮箱账号；第二个参数是个列表类型，每个元素为一个接收者；第三个参数：邮件内容
    #         smtp_server.quit()  # 发送完成后加上这个函数调用，类似于open文件后要跟一个close文件一样
    except Exception as e:
        print(e)


# 作为示例的 html文本
html = requests.get("https://www.ip.cn/").text
soup = BeautifulSoup(html, 'lxml')
# 对 html文本进行处理 获得一个_Element对象
nodes = soup.select('p > span.cf-footer-item ')
# 获取
for node in nodes:
    text = node.get_text()
    if text.startswith('Your IP'):
        ip = text.split(':')[1].strip()
        send(ip)
