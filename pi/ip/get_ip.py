import smtplib
import time
from email.header import Header
from email.mime.text import MIMEText
from email.utils import formataddr

import requests
from bs4 import BeautifulSoup


def sendEmail(mail_content, recive_email):
    """

    :param mail_content: 邮件内容
    :param recive_email: 邮件接收者
    :return:
    """
    try:
        from_addr = '18758298536@163.com'
        msg = MIMEText(mail_content, 'plain', 'utf-8')
        msg['From'] = formataddr(['树莓派', '18758298536@163.com'])
        msg['To'] = formataddr(['blanexie ', recive_email])
        msg['Subject'] = Header('获取我的树莓派ip地址', 'utf-8').encode()
        server = smtplib.SMTP_SSL('smtp.163.com', 465)
        server.set_debuglevel(1)
        server.login(from_addr, 'ESMEQBOJGJKHZCYD')
        server.sendmail(from_addr, [recive_email], msg.as_string())
        server.quit()
    except Exception as e:
        print(e)


def getip():
    # 作为示例的 html文本
    html = requests.get("https://www.ip.cn/").text
    soup = BeautifulSoup(html, 'html.parser')
    # 对 html文本进行处理 获得一个_Element对象
    nodes = soup.select('p > span.cf-footer-item ')
    # 获取
    for node in nodes:
        text = node.get_text()
        if text.startswith('Your IP'):
            return text.split(':')[1].strip()


if __name__ == "__main__":
    # execute only if run as a script
    ip = getip()
    with open("./ip.txt", 'w+') as f:
        ip_old = f.read()
        if ip != ip_old:
            dt = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))
            sendEmail('时间：{}\n ip：{}'.format(dt, ip), 'blanexie@qq.com')
            f.write(ip)
