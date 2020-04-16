import smtplib
from email.header import Header
from email.mime.text import MIMEText
from email.utils import formataddr
import time

import requests
from bs4 import BeautifulSoup


def sendEmail(ip):
    try:
        dt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        from_addr = '18758298536@163.com'
        to_addr = 'blanexie@qq.com'
        msg = MIMEText('{} : {}'.format(dt, ip), 'plain', 'utf-8')
        msg['From'] = formataddr(['树莓派', '18758298536@163.com'])
        msg['To'] = formataddr(['blanexie ', 'blanexie@qq.com'])
        msg['Subject'] = Header('获取我的树莓派ip地址', 'utf-8').encode()
        server = smtplib.SMTP_SSL('smtp.163.com', 465)
        server.set_debuglevel(1)
        server.login(from_addr, 'ESMEQBOJGJKHZCYD')
        server.sendmail(from_addr, [to_addr], msg.as_string())
        server.quit()
    except Exception as e:
        print(e)


def getip():
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
            return ip


if __name__ == "__main__":
    # execute only if run as a script
    ip = getip()
    sendEmail(ip)

