from exts import db


class FzuClass(db.Model):
    __tablename__ = 'fzu_class'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(32), nullable=False)

    def __init__(self, name):
        self.name = name


class User(db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    student_number = db.Column(db.String(64), index=True, unique=True, nullable=False)
    password = db.Column(db.String(64), nullable=False)
    name = db.Column(db.String(64), nullable=False)
    class_master = db.Column(db.Boolean, nullable=False)
    class_id = db.Column(db.Integer, db.ForeignKey('fzu_class.id'))

    def __init__(self, student_number, password, name):
        self.class_master = False
        self.student_number = student_number
        self.password = password
        self.name = name

    def __repr__(self):
        return '<用户名:{}>'.format(self.name)


class Mark(db.Model):
    __tablename__ = 'mark'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    mark = db.Column(db.Float, nullable=False)
    name = db.Column(db.String(32), nullable=False)
    grade = db.Column(db.String(16), nullable=False)
    rank = db.Column(db.Integer)
    rank_rate = db.Column(db.Float)
    class_rank = db.Column(db.Integer)
    class_rank_rate = db.Column(db.Float)
    segment = db.Column(db.Integer)
    average = db.Column(db.Float)

    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    class_id = db.Column(db.Integer, db.ForeignKey('fzu_class.id'))

    def __init__(self, mark, name, class_id, user_id):
        self.name = name
        self.mark = mark
        self.class_id = class_id
        self.user_id = user_id


class Class_mark(db.Model):
    __tablename__ = "class_mark"
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(32), nullable=False)
    good_rate = db.Column(db.Float)
    pass_rate = db.Column(db.Float)
    average = db.Column(db.Float)
    max_s = db.Column(db.Float)
    min_s = db.Column(db.Float)
    grade = db.Column(db.String(16), nullable=False)
    class_id = db.Column(db.Integer, db.ForeignKey('fzu_class.id'))

    def __init__(self, name, average, max_s, min_s, pass_rate, good_rate, grade, class_id):
        self.name = name
        self.average = average
        self.max_s = max_s
        self.min_s = min_s
        self.pass_rate = pass_rate
        self.good_rate = good_rate
        self.grade = grade
        self.class_id = class_id