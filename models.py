from exts import db
from flask_login import UserMixin
from hashlib import md5
import random


class User(UserMixin, db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    student_number = db.Column(db.String(64), index=True, unique=True, nullable=False)
    password = db.Column(db.String(64), nullable=False)
    name = db.Column(db.String(64), nullable=False)
    class_master = db.Column(db.Boolean, nullable=False)

    def __init__(self, student_number, password, name):
        self.class_master = False
        self.student_number = student_number
        self.password = password
        self.name = name

    def __repr__(self):
        return '<用户名:{}>'.format(self.username)

    def get_avatar(self, size=256):
        styles = ['identicon', 'monsterid', 'wavatar']
        random_str = ''.join([chr(random.randint(0x0000, 0x9fbf)) for _ in range(random.randint(1, 25))])
        m = md5("{}".format(random_str).encode("utf-8")).hexdigest()
        return r'http://www.gravatar.com/avatar/{}?s={}&d={}'.format(m, size, random.choice(styles))


class Mark(db.Model):
    __tablename__ = 'mark'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    mark = db.Column(db.Float, nullable=False)
    name = db.Column(db.String(32), nullable=False)
    grade = db.Column(db.String(16), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    user = db.relationship('User', backref=db.backref('Mark'))
    rank = db.Column(db.Integer)
    rank_rate = db.Column(db.Float)
    class_rank = db.Column(db.Integer)
    mean = db.Column(db.Float)

    def __init__(self, mark, name):
        self.name = name
        self.mark = mark


class Class_mark(db.Model):
    __tablename__ = "class_mark"
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(32), nullable=False)
    good_rate = db.Column(db.Float)
    pass_rate = db.Column(db.Float)
    mean = db.Column(db.Float)
    max_s = db.Column(db.Float)
    min_s = db.Column(db.Float)
    grade = db.Column(db.String(16), nullable=False)

    def __init__(self, name, mean, max_s, min_s, pass_rate, good_rate):
        self.name = name
        self.mean = mean
        self.max_s = max_s
        self.min_s = min_s
        self.pass_rate = pass_rate
        self.good_rate = good_rate


