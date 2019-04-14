import os


class Config(object):
    BASEPATH = os.getcwd().replace('\\', r'\\')
    SECRET_KEY = os.urandom(24)
    # 格式为mysql+pymysql://数据库用户名:密码@数据库地址:端口号/数据库的名字?数据库格式
    SQLALCHEMY_DATABASE_URI = 'mysql+pymysql://root:@localhost:3306/hackathon?charset=utf8'
    SQLALCHEMY_TRACK_MODIFICATIONS = False


